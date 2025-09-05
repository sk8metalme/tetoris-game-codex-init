package com.example.tetoris.application;

import com.example.tetoris.domain.model.Board;
import com.example.tetoris.domain.model.GameState;
import com.example.tetoris.domain.model.Piece;
import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** メモリ内ゲームセッション管理（MVP）。 - 単純なOブロックをスポーンし、基本操作のみ適用。 */
public class GameSessionService {

  public record Session(GameState state, int rev, int score, int combo) {}

  private final Map<String, Session> sessions = new ConcurrentHashMap<>();
  private final Map<String, String> startKeyToId = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Session>> idempotencyBySession = new ConcurrentHashMap<>();
  private final Map<String, com.example.tetoris.domain.random.TetrominoGenerator> gens =
      new ConcurrentHashMap<>();

  public static class NotFoundException extends RuntimeException {
    public NotFoundException(String id) {
      super("game not found: " + id);
    }
  }

  public String startGame(Optional<Integer> widthOpt, Optional<Integer> heightOpt) {
    int w = widthOpt.orElse(10);
    int h = heightOpt.orElse(20);
    Size size = Size.of(w, h);
    Board board = GridBoard.empty(size);
    Piece current = oBlockAt(w / 2, 0);
    GameState state = GameState.of(board, current);
    String id = UUID.randomUUID().toString();
    sessions.put(id, new Session(state, 0, 0, 0));
    // 7-bag 生成器を初期化（MVP: シードはtimeベース）
    gens.put(id, com.example.tetoris.domain.random.Generators.sevenBag(System.nanoTime()));
    return id;
  }

  /** オプション付き開始（幅・高さ・seed・difficulty）。difficulty=O_ONLY で定数Oミノ。 */
  public String startGameWithOptions(
      Optional<Integer> widthOpt,
      Optional<Integer> heightOpt,
      Optional<Long> seedOpt,
      Optional<String> difficultyOpt) {
    String id = startGame(widthOpt, heightOpt);
    // Generator を上書き
    var diff = difficultyOpt.map(String::toUpperCase).orElse("");
    if ("O_ONLY".equals(diff)) {
      gens.put(id, new com.example.tetoris.domain.random.impl.ConstantGenerator(
          com.example.tetoris.domain.value.TetrominoType.O));
    } else if (seedOpt.isPresent()) {
      gens.put(id, new com.example.tetoris.domain.random.impl.SevenBagGenerator(seedOpt.get()));
    }
    return id;
  }

  /** Idempotent start: 同一キーなら同一IDを返す。 */
  public String startGameIdempotent(
      Optional<Integer> widthOpt, Optional<Integer> heightOpt, Optional<String> idempotencyKey) {
    if (idempotencyKey.isPresent()) {
      String key = idempotencyKey.get();
      String existing = startKeyToId.get(key);
      if (existing != null) {
        return existing;
      }
      String id = startGame(widthOpt, heightOpt);
      startKeyToId.put(key, id);
      return id;
    }
    return startGame(widthOpt, heightOpt);
  }

  public String startGameIdempotentWithOptions(
      Optional<Integer> widthOpt,
      Optional<Integer> heightOpt,
      Optional<Long> seedOpt,
      Optional<String> difficultyOpt,
      Optional<String> idempotencyKey) {
    if (idempotencyKey.isPresent()) {
      String key = idempotencyKey.get();
      String existing = startKeyToId.get(key);
      if (existing != null) return existing;
      String id = startGameWithOptions(widthOpt, heightOpt, seedOpt, difficultyOpt);
      startKeyToId.put(key, id);
      return id;
    }
    return startGameWithOptions(widthOpt, heightOpt, seedOpt, difficultyOpt);
  }

  public Session get(String id) {
    Session s = sessions.get(id);
    if (s == null) throw new NotFoundException(id);
    return s;
  }

  public Session apply(String id, String action, int repeat) {
    Session s = get(id);
    GameState st = s.state();
    int score = s.score();
    int combo = s.combo();
    for (int i = 0; i < Math.max(1, repeat); i++) {
      st =
          switch (action) {
            case "MOVE_LEFT" -> st.moveLeft();
            case "MOVE_RIGHT" -> st.moveRight();
            case "SOFT_DROP" -> st.softDrop();
            case "HARD_DROP" -> st.hardDrop();
            case "ROTATE_CW" -> st.rotateCW();
            case "ROTATE_CCW" -> st.rotateCCW();
            case "LOCK" -> {
              var before = st;
              var res = st.board().placeAndClear(st.current());
              // スコア計算
              var rule = new com.example.tetoris.domain.rules.impl.BasicScoreRule();
              var lc = res.lineClear();
              if (lc != com.example.tetoris.domain.value.LineClearType.NONE) {
                score += rule.lineClearScore(lc) + rule.comboBonus(combo);
                combo += 1;
              } else {
                combo = 0;
              }
              // 次ミノスポーン（7-bag）
              com.example.tetoris.domain.random.TetrominoGenerator g = gens.get(id);
              com.example.tetoris.domain.value.TetrominoType type =
                  g != null ? g.next() : com.example.tetoris.domain.value.TetrominoType.O;
              var srs = new com.example.tetoris.domain.rules.srs.SrsRotationSystem();
              var pos = srs.spawnPosition(before.board().size(), type);
              var next =
                  new com.example.tetoris.domain.model.impl.RotatingPiece(
                      type, com.example.tetoris.domain.value.Rotation.R0, pos.x(), pos.y());
              st = GameState.of(res.board(), next);
              yield st;
            }
            default -> st; // 未対応は無変化
          };
    }
    Session updated = new Session(st, s.rev() + 1, score, combo);
    sessions.put(id, updated);
    return updated;
  }

  /** Idempotent apply: 同一キーなら同じ結果（rev/state）を返す。 */
  public Session applyIdempotent(String id, String idempotencyKey, String action, int repeat) {
    Map<String, Session> m =
        idempotencyBySession.computeIfAbsent(id, k -> new ConcurrentHashMap<>());
    Session prev = m.get(idempotencyKey);
    if (prev != null) return prev;
    Session res = apply(id, action, repeat);
    m.put(idempotencyKey, res);
    return res;
  }

  public void delete(String id) {
    sessions.remove(id);
    gens.remove(id);
  }

  private static Piece oBlockAt(int x, int y) {
    // 2x2 O ミノ（RotatingPiece: Oは全向き同形状）
    return new com.example.tetoris.domain.model.impl.RotatingPiece(
        com.example.tetoris.domain.value.TetrominoType.O,
        com.example.tetoris.domain.value.Rotation.R0,
        x,
        y);
  }

  /** ピースを盤面にロックし、行消去後に新規ピースをスポーンする（7-bag使用）。 */
  private GameState lock(String id, GameState st) {
    // ロック＆消去
    Board.PlaceResult res = st.board().placeAndClear(st.current());
    // 次ミノ種別を7-bagから取得（なければO）
    com.example.tetoris.domain.random.TetrominoGenerator g = gens.get(id);
    com.example.tetoris.domain.value.TetrominoType type =
        g != null ? g.next() : com.example.tetoris.domain.value.TetrominoType.O;
    var srs = new com.example.tetoris.domain.rules.srs.SrsRotationSystem();
    var pos = srs.spawnPosition(st.board().size(), type);
    Piece next =
        new com.example.tetoris.domain.model.impl.RotatingPiece(
            type, com.example.tetoris.domain.value.Rotation.R0, pos.x(), pos.y());
    return GameState.of(res.board(), next);
  }
}
