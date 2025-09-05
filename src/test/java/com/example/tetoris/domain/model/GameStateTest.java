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

class GameStateTest {

  @Test
  @DisplayName("moveLeft: 左が空ならピースが1マス左へ移動する")
  void moveLeft_movesOneCellLeft_whenVacant() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);

    // 2x2の塊を (5,0),(6,0),(5,1),(6,1) に配置するピース（絶対座標）
    FakeOffsetPiece piece = FakeOffsetPiece.oBlockAt(5, 0);
    GameState state = GameState.of(board, piece);

    GameState moved = state.moveLeft();

    // 期待: 左へ1マス (4,0),(5,0),(4,1),(5,1)
    Set<Position> expected =
        setOf(new Position(4, 0), new Position(5, 0), new Position(4, 1), new Position(5, 1));
    assertEquals(expected, new HashSet<>(moved.current().cells()), "左に1マス移動しているべき");
  }

  @Test
  @DisplayName("moveRight: 右が空ならピースが1マス右へ移動する")
  void moveRight_movesOneCellRight_whenVacant() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);

    FakeOffsetPiece piece = FakeOffsetPiece.oBlockAt(5, 0);
    GameState state = GameState.of(board, piece);

    GameState moved = state.moveRight();

    Set<Position> expected =
        setOf(new Position(6, 0), new Position(7, 0), new Position(6, 1), new Position(7, 1));
    assertEquals(expected, new HashSet<>(moved.current().cells()), "右に1マス移動しているべき");
  }

  @Test
  @DisplayName("softDrop: 下が空ならピースが1マス下へ移動する")
  void softDrop_movesOneCellDown_whenVacant() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);

    FakeOffsetPiece piece = FakeOffsetPiece.oBlockAt(5, 0);
    GameState state = GameState.of(board, piece);

    GameState moved = state.softDrop();

    Set<Position> expected =
        setOf(new Position(5, 1), new Position(6, 1), new Position(5, 2), new Position(6, 2));
    assertEquals(expected, new HashSet<>(moved.current().cells()), "下に1マス移動しているべき");
  }

  @Test
  @DisplayName("hardDrop: 接地まで一気に落下する（空盤面で底まで）")
  void hardDrop_dropsToBottom_onEmptyBoard() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);

    FakeOffsetPiece piece = FakeOffsetPiece.oBlockAt(5, 0);
    GameState state = GameState.of(board, piece);

    GameState dropped = state.hardDrop();

    // Oブロック（2x2）が底に接する配置: 上段y=18, 下段y=19
    Set<Position> expected =
        setOf(new Position(5, 18), new Position(6, 18), new Position(5, 19), new Position(6, 19));
    assertEquals(expected, new HashSet<>(dropped.current().cells()), "底まで落下しているべき");
  }

  // テスト専用：原点(0,0)周りのO字形をオフセットで動かす簡易ピース
  static class FakeOffsetPiece implements Piece {
    private final int ox, oy;

    private FakeOffsetPiece(int ox, int oy) {
      this.ox = ox;
      this.oy = oy;
    }

    static FakeOffsetPiece oBlockAt(int x, int y) {
      return new FakeOffsetPiece(x, y);
    }

    @Override
    public List<Position> cells() {
      List<Position> c = new ArrayList<>();
      c.add(new Position(ox, oy));
      c.add(new Position(ox + 1, oy));
      c.add(new Position(ox, oy + 1));
      c.add(new Position(ox + 1, oy + 1));
      return c;
    }
  }

  private static Set<Position> setOf(Position... ps) {
    Set<Position> s = new HashSet<>();
    for (Position p : ps) s.add(p);
    return s;
  }
}
