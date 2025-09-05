package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.value.LineClearType;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardPlaceAndClearEdgeTest {

  @Test
  @DisplayName("placeAndClear: 3行同時消去はTRIPLE")
  void triple_clear() {
    Size size = Size.of(10, 6);
    boolean[][] g = new boolean[size.height()][size.width()];
    // 下から3行(y=3,4,5)を各1セルだけ空ける（x=9を空ける）
    for (int y = 3; y <= 5; y++) {
      for (int x = 0; x < 10; x++) {
        if (x == 9) continue;
        g[y][x] = true;
      }
    }
    Board board = GridBoard.fromBooleans(size, g);

    // 空いている3セル (9,3),(9,4),(9,5) を埋めるピース
    Piece piece =
        new FakePiece(List.of(new Position(9, 3), new Position(9, 4), new Position(9, 5)));

    assertTrue(board.canPlace(piece));
    Board.PlaceResult r = board.placeAndClear(piece);
    assertEquals(LineClearType.TRIPLE, r.lineClear());
  }

  @Test
  @DisplayName("placeAndClear: 4行同時消去はTETRIS")
  void tetris_clear() {
    Size size = Size.of(10, 6);
    boolean[][] g = new boolean[size.height()][size.width()];
    // 下から4行(y=2..5)を各1セルだけ空ける
    for (int y = 2; y <= 5; y++) {
      for (int x = 0; x < 10; x++) {
        if (x == 9) continue;
        g[y][x] = true;
      }
    }
    Board board = GridBoard.fromBooleans(size, g);

    Piece piece =
        new FakePiece(
            List.of(
                new Position(9, 2), new Position(9, 3), new Position(9, 4), new Position(9, 5)));

    assertTrue(board.canPlace(piece));
    Board.PlaceResult r = board.placeAndClear(piece);
    assertEquals(LineClearType.TETRIS, r.lineClear());
  }

  @Test
  @DisplayName("placeAndClear: 重なり/外には置けず、副作用なしでNONE")
  void reject_overlap_and_oob() {
    Size size = Size.of(4, 4);
    boolean[][] g = new boolean[size.height()][size.width()];
    g[3][0] = true; // 既存ブロック
    Board board = GridBoard.fromBooleans(size, g);

    // 既存ブロックに重なる
    Piece overlap = new FakePiece(List.of(new Position(0, 3)));
    Board.PlaceResult r1 = board.placeAndClear(overlap);
    assertSame(board, r1.board());
    assertEquals(LineClearType.NONE, r1.lineClear());

    // 盤外（負座標と右外）
    Piece oob = new FakePiece(List.of(new Position(-1, 0), new Position(4, 1)));
    Board.PlaceResult r2 = board.placeAndClear(oob);
    assertSame(board, r2.board());
    assertEquals(LineClearType.NONE, r2.lineClear());
  }

  // テスト専用: 与えた絶対座標集合をそのまま返すピース
  static class FakePiece implements Piece {
    private final List<Position> cells;

    FakePiece(List<Position> cells) {
      this.cells = cells;
    }

    @Override
    public List<Position> cells() {
      return cells;
    }
  }
}
