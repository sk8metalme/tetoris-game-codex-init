package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.value.LineClearType;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardPlaceAndClearDoubleTest {
  @Test
  @DisplayName("二行同時消去でDOUBLEが返る")
  void placeAndClear_doubleLine() {
    Size size = Size.of(10, 4);
    boolean[][] g = new boolean[size.height()][size.width()];
    for (int x = 0; x < 9; x++) {
      g[2][x] = true;
      g[3][x] = true;
    }
    Board board = GridBoard.fromBooleans(size, g);

    // 行y=2とy=3の不足セル(9,*)を埋める + 影響のない別行に2セル
    Piece piece =
        new FakePiece(
            List.of(
                new Position(9, 2), new Position(9, 3), new Position(0, 0), new Position(0, 1)));

    Board.PlaceResult r = board.placeAndClear(piece);
    assertEquals(LineClearType.DOUBLE, r.lineClear());
  }

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
