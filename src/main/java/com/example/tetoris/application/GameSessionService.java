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

  public record Session(GameState state, int rev) {}

  private final Map<String, Session> sessions = new ConcurrentHashMap<>();
  private final Map<String, String> startKeyToId = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Session>> idempotencyBySession = new ConcurrentHashMap<>();

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
    sessions.put(id, new Session(state, 0));
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

  public Session get(String id) {
    Session s = sessions.get(id);
    if (s == null) throw new NotFoundException(id);
    return s;
  }

  public Session apply(String id, String action, int repeat) {
    Session s = get(id);
    GameState st = s.state();
    for (int i = 0; i < Math.max(1, repeat); i++) {
      st =
          switch (action) {
            case "MOVE_LEFT" -> st.moveLeft();
            case "MOVE_RIGHT" -> st.moveRight();
            case "SOFT_DROP" -> st.softDrop();
            case "HARD_DROP" -> st.hardDrop();
            case "ROTATE_CW" -> st.rotateCW();
            case "ROTATE_CCW" -> st.rotateCCW();
            case "LOCK" -> lock(st);
            default -> st; // 未対応は無変化
          };
    }
    Session updated = new Session(st, s.rev() + 1);
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
  }

  private static Piece oBlockAt(int x, int y) {
    // 2x2 O ミノ（RotatingPiece: Oは全向き同形状）
    return new com.example.tetoris.domain.model.impl.RotatingPiece(
        com.example.tetoris.domain.value.TetrominoType.O,
        com.example.tetoris.domain.value.Rotation.R0,
        x,
        y);
  }

  /** ピースを盤面にロックし、行消去後に新規ピースをスポーンする（MVP: O固定）。 */
  private GameState lock(GameState st) {
    Board.PlaceResult res = st.board().placeAndClear(st.current());
    int w = st.board().size().width();
    Piece next = oBlockAt(w / 2, 0);
    return GameState.of(res.board(), next);
  }
}
