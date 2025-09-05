package com.example.tetoris.application;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.GameState;
import com.example.tetoris.domain.value.Position;
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
