package com.example.tetoris.application;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.GameState;
import com.example.tetoris.domain.model.impl.RotatingPiece;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameSessionServiceTest {

  @Test
  @DisplayName("startGame: 既定サイズ10x20でrev=0, Oピース初期位置")
  void startGame_default() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.empty(), Optional.empty());
    GameSessionService.Session s = svc.get(id);

    assertEquals(0, s.rev());
    GameState st = s.state();
    assertEquals(10, st.board().size().width());
    assertEquals(20, st.board().size().height());

    // Oピースの初期位置は (w/2, 0) を含む 2x2
    List<Position> cells = st.current().cells();
    assertTrue(
        cells.stream().anyMatch(p -> p.x() == 5 && p.y() == 0),
        "should contain top-left at (5,0) when width=10");
    assertEquals(4, cells.size());
  }

  @Test
  @DisplayName("startGameIdempotent: 同一Idempotency-Keyで同じIDを返す")
  void startGame_idempotent() {
    GameSessionService svc = new GameSessionService();
    String key = "k1";
    String id1 = svc.startGameIdempotent(Optional.of(8), Optional.of(16), Optional.of(key));
    String id2 = svc.startGameIdempotent(Optional.of(8), Optional.of(16), Optional.of(key));
    assertEquals(id1, id2);
  }

  @Test
  @DisplayName("回転アクション: ROTATE_CW/CCW でrevが進む（Oは形状不変でもOK）")
  void rotate_actions_advance_revision() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.empty(), Optional.empty());
    GameSessionService.Session s1 = svc.apply(id, "ROTATE_CW", 1);
    assertEquals(1, s1.rev());
    GameSessionService.Session s2 = svc.apply(id, "ROTATE_CCW", 1);
    assertEquals(2, s2.rev());
  }

  @Test
  @DisplayName("apply: MOVE_LEFT と HARD_DROP で座標とrevが更新される")
  void apply_movesAndDrop() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.of(10), Optional.of(20));
    GameSessionService.Session s1 = svc.apply(id, "MOVE_LEFT", 1);
    assertEquals(1, s1.rev());
    int minXAfterLeft =
        s1.state().current().cells().stream().mapToInt(Position::x).min().orElse(-1);
    assertEquals(4, minXAfterLeft); // 初期5から左へ1

    GameSessionService.Session s2 = svc.apply(id, "HARD_DROP", 1);
    assertEquals(2, s2.rev());
    int minYAfterDrop =
        s2.state().current().cells().stream().mapToInt(Position::y).min().orElse(-1);
    // Oピースは2マス高いので topY は height-2 (=18)
    assertEquals(18, minYAfterDrop);
  }

  @Test
  @DisplayName("applyIdempotent: 同一キーで同じ結果(rev/state)を返す")
  void apply_idempotent() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.empty(), Optional.empty());
    GameSessionService.Session a = svc.applyIdempotent(id, "ix", "SOFT_DROP", 3);
    GameSessionService.Session b = svc.applyIdempotent(id, "ix", "SOFT_DROP", 3);
    assertSame(a, b);
    assertEquals(a.rev(), b.rev());
    assertEquals(
        a.state().current().cells(),
        b.state().current().cells(),
        "idempotent apply returns identical state");
  }

  @Test
  @DisplayName("apply: 未知アクションは無変化だがrevは+1（default分岐）")
  void apply_unknown_action_noop() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.empty(), Optional.empty());
    GameSessionService.Session before = svc.get(id);
    var cellsBefore = List.copyOf(before.state().current().cells());

    GameSessionService.Session after = svc.apply(id, "UNKNOWN", 1);
    assertEquals(before.rev() + 1, after.rev());
    assertEquals(cellsBefore, after.state().current().cells());
  }

  @Test
  @DisplayName("LOCK: hardDrop後にLOCKで盤面に固定され、新規Oがスポーンする")
  void lock_after_hard_drop_spawns_new_piece_and_fixes_board() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.of(10), Optional.of(20));

    // 1) ハードドロップ（底まで移動）
    GameSessionService.Session s1 = svc.apply(id, "HARD_DROP", 1);
    // ドロップ後のピース下段は y=19 のはず（O: 高さ2）
    int maxY = s1.state().current().cells().stream().mapToInt(Position::y).max().orElse(-1);
    assertEquals(19, maxY);

    // 2) LOCK で盤面に固定 + 新規スポーン
    GameSessionService.Session s2 = svc.apply(id, "LOCK", 1);
    // 新規ピースは上部にスポーン（y=0 を含む）
    int minYNew = s2.state().current().cells().stream().mapToInt(Position::y).min().orElse(-1);
    assertEquals(0, minYNew);
    // 盤面固定セル数は4になる（Oが1つロック）
    long locked =
        s2.state().board().occupancy().stream()
            .flatMap(List::stream)
            .filter(Boolean::booleanValue)
            .count();
    assertEquals(4, locked);
  }

  @Test
  @DisplayName("スコア: DOUBLEで300加算、連続DOUBLEで次は+300+コンボ50")
  void scoring_double_then_combo() throws Exception {
    GameSessionService svc = new GameSessionService();
    // 盤面準備: 幅10, 高さ6
    String id = svc.startGame(Optional.of(10), Optional.of(6));

    // セッションのボードを2段×2回分、各行のx=8,9だけ空ける形で事前埋め
    GameSessionService.Session s0 = svc.get(id);
    var size = s0.state().board().size();
    boolean[][] g = new boolean[size.height()][size.width()];
    // 下2行 y=5,4 と その上 2行 y=3,2 を x=0..7 を埋める
    for (int y : new int[] {5, 4, 3, 2}) {
      for (int x = 0; x < 8; x++) g[y][x] = true;
    }
    // 新しいボードを差し替える（テスト用簡易差し替え）
    var board = com.example.tetoris.domain.model.impl.GridBoard.fromBooleans(size, g);
    // 現在ピースはそのまま流用
    var gs = com.example.tetoris.domain.model.GameState.of(board, s0.state().current());
    // セッションに直接反映
    var field = GameSessionService.class.getDeclaredField("sessions");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, GameSessionService.Session> map =
        (java.util.Map<String, GameSessionService.Session>) field.get(svc);
    map.put(id, new GameSessionService.Session(gs, s0.rev(), s0.score(), s0.combo()));

    // 次ミノを常にOへ固定して再現性を担保
    var gfield = GameSessionService.class.getDeclaredField("gens");
    gfield.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, com.example.tetoris.domain.random.TetrominoGenerator> gmap =
        (java.util.Map<String, com.example.tetoris.domain.random.TetrominoGenerator>)
            gfield.get(svc);
    gmap.put(
        id, new com.example.tetoris.domain.random.impl.ConstantGenerator(TetrominoType.O));

    // 1回目: 右へ移動して (x=8,9) を埋める → DOUBLE (300)
    svc.apply(id, "MOVE_RIGHT", 3); // 初期アンカー5→8
    svc.apply(id, "HARD_DROP", 1);
    GameSessionService.Session s1 = svc.apply(id, "LOCK", 1);
    assertEquals(300, s1.score());

    // 2回目: 次のO（spawnPositionでx=3）を右へ移動 5 回して (x=8,9) を埋める → DOUBLE + combo(50)
    svc.apply(id, "MOVE_RIGHT", 5); // 3→8
    svc.apply(id, "HARD_DROP", 1);
    GameSessionService.Session s2 = svc.apply(id, "LOCK", 1);
    assertEquals(650, s2.score()); // 300 + (300+50)
  }

  @Test
  @DisplayName("スコア: クリア無しのLOCKでスコア据え置き・コンボリセット、fallback Oがスポーン")
  void lock_none_resets_combo_and_spawns_fallback_O() throws Exception {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.of(10), Optional.of(4));
    GameSessionService.Session s0 = svc.get(id);

    // sessions へ直接反映して combo/score を擬似的に設定、gens は削除して fallback O を通す
    var sessionsF = GameSessionService.class.getDeclaredField("sessions");
    sessionsF.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, GameSessionService.Session> smap =
        (java.util.Map<String, GameSessionService.Session>) sessionsF.get(svc);
    smap.put(id, new GameSessionService.Session(s0.state(), s0.rev(), 123, 2));

    var gensF = GameSessionService.class.getDeclaredField("gens");
    gensF.setAccessible(true);
    @SuppressWarnings("unchecked")
    java.util.Map<String, com.example.tetoris.domain.random.TetrominoGenerator> gmap =
        (java.util.Map<String, com.example.tetoris.domain.random.TetrominoGenerator>)
            gensF.get(svc);
    gmap.remove(id);

    // LOCK（行消去は発生しない）
    GameSessionService.Session s1 = svc.apply(id, "LOCK", 1);
    assertEquals(123, s1.score());
    assertEquals(0, s1.combo());
    // 次ミノは fallback で O
    RotatingPiece p = (RotatingPiece) s1.state().current();
    assertEquals(TetrominoType.O, p.type());
    // 盤面には4セル固定されている
    long locked =
        s1.state().board().occupancy().stream()
            .flatMap(List::stream)
            .filter(Boolean::booleanValue)
            .count();
    assertEquals(4, locked);
  }

  @Test
  @DisplayName("7-bag: 複数回のLOCKでO以外のミノがスポーンする")
  void seven_bag_spawns_non_O_after_some_locks() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.of(10), Optional.of(20));
    boolean seenNonO = false;
    for (int i = 0; i < 8; i++) { // 7-bag以内に必ずO以外が出るはず
      svc.apply(id, "HARD_DROP", 1);
      GameSessionService.Session s = svc.apply(id, "LOCK", 1);
      RotatingPiece p = (RotatingPiece) s.state().current();
      if (p.type() != TetrominoType.O) {
        seenNonO = true;
        break;
      }
    }
    assertTrue(seenNonO, "7-bag により O 以外がスポーンするはず");
  }

  @Test
  @DisplayName("apply: repeatが負でも1回として扱われる（Math.max分岐）")
  void apply_negative_repeat_treated_as_one() {
    GameSessionService svc = new GameSessionService();
    String id = svc.startGame(Optional.empty(), Optional.empty());
    GameSessionService.Session before = svc.get(id);
    GameSessionService.Session after = svc.apply(id, "SOFT_DROP", -10);
    // rev は +1 のはず（1回のみ適用）
    assertEquals(before.rev() + 1, after.rev());
  }

  @Test
  @DisplayName("get: 不正IDはNotFoundException")
  void get_notFound() {
    GameSessionService svc = new GameSessionService();
    assertThrows(GameSessionService.NotFoundException.class, () -> svc.get("nope"));
  }
}
