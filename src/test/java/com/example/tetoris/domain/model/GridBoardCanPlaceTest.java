package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GridBoardCanPlaceTest {

  @Test
  @DisplayName("canPlace: 盤外セルが含まれるとfalse")
  void canPlace_returnsFalse_when_out_of_bounds() {
    Size size = Size.of(4, 4);
    Board board = GridBoard.empty(size);
    Piece oob = new FixedPiece(List.of(new Position(-1, 0), new Position(0, 0)));
    assertFalse(board.canPlace(oob));
  }

  @Test
  @DisplayName("canPlace: 占有セルと重なるとfalse")
  void canPlace_returnsFalse_when_overlap() {
    Size size = Size.of(4, 4);
    boolean[][] g = new boolean[size.height()][size.width()];
    g[1][1] = true;
    Board board = GridBoard.fromBooleans(size, g);
    Piece overlap = new FixedPiece(List.of(new Position(1, 1)));
    assertFalse(board.canPlace(overlap));
  }

  static class FixedPiece implements Piece {
    private final List<Position> cells;

    FixedPiece(List<Position> cells) {
      this.cells = cells;
    }

    @Override
    public List<Position> cells() {
      return cells;
    }
  }
}
