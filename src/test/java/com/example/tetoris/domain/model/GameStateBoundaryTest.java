package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameStateBoundaryTest {

  @Test
  @DisplayName("moveLeft: 左端では移動しない（board.canPlace=false 分岐）")
  void moveLeft_blocked_at_left_edge() {
    Size size = Size.of(4, 4);
    Board board = GridBoard.empty(size);
    // O 2x2 を (0,0) に置く
    Piece piece = oBlockAt(0, 0);
    GameState s = GameState.of(board, piece);

    GameState moved = s.moveLeft();
    assertEquals(
        setOf(new Position(0, 0), new Position(1, 0), new Position(0, 1), new Position(1, 1)),
        new HashSet<>(moved.current().cells()));
  }

  @Test
  @DisplayName("moveRight: 右端では移動しない")
  void moveRight_blocked_at_right_edge() {
    Size size = Size.of(4, 4);
    Board board = GridBoard.empty(size);
    // O を (2,0) に置く（右端2x2）
    Piece piece = oBlockAt(2, 0);
    GameState s = GameState.of(board, piece);

    GameState moved = s.moveRight();
    assertEquals(
        setOf(new Position(2, 0), new Position(3, 0), new Position(2, 1), new Position(3, 1)),
        new HashSet<>(moved.current().cells()));
  }

  @Test
  @DisplayName("softDrop: 直下に障害物があると落ちない")
  void softDrop_blocked_by_occupancy() {
    Size size = Size.of(6, 6);
    boolean[][] g = new boolean[size.height()][size.width()];
    // y=2 をすべて埋める
    for (int x = 0; x < size.width(); x++) g[2][x] = true;
    Board board = GridBoard.fromBooleans(size, g);
    Piece piece = oBlockAt(2, 0); // 下に進むと y=2 に衝突
    GameState s = GameState.of(board, piece);

    GameState moved = s.softDrop();
    assertEquals(
        setOf(new Position(2, 0), new Position(3, 0), new Position(2, 1), new Position(3, 1)),
        new HashSet<>(moved.current().cells()));
  }

  @Test
  @DisplayName("hardDrop: 障害物の直上で停止する")
  void hardDrop_stops_above_obstacle() {
    Size size = Size.of(10, 12);
    boolean[][] g = new boolean[size.height()][size.width()];
    // y=10 を全埋め
    for (int x = 0; x < size.width(); x++) g[10][x] = true;
    Board board = GridBoard.fromBooleans(size, g);
    Piece piece = oBlockAt(5, 0);
    GameState s = GameState.of(board, piece);

    GameState dropped = s.hardDrop();
    // O は2高 → 上段が y=8 で止まる
    assertTrue(dropped.current().cells().stream().anyMatch(p -> p.y() == 8));
    assertTrue(dropped.current().cells().stream().anyMatch(p -> p.y() == 9));
  }

  private static Piece oBlockAt(int x, int y) {
    return new Piece() {
      @Override
      public List<Position> cells() {
        List<Position> c = new ArrayList<>();
        c.add(new Position(x, y));
        c.add(new Position(x + 1, y));
        c.add(new Position(x, y + 1));
        c.add(new Position(x + 1, y + 1));
        return c;
      }
    };
  }

  private static Set<Position> setOf(Position... ps) {
    Set<Position> s = new HashSet<>();
    for (Position p : ps) s.add(p);
    return s;
  }
}
