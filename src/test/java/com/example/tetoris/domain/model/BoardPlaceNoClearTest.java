package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.value.LineClearType;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardPlaceNoClearTest {

  @Test
  @DisplayName("placeAndClear: ライン完成が無ければNONE（toLineClearTypeのdefault分岐）")
  void place_no_line_clear_returns_NONE() {
    Size size = Size.of(6, 4);
    Board board = GridBoard.empty(size);

    // 4セルをばらして配置（いずれの行も6/6未満）
    Piece piece =
        new FakePiece(
            List.of(
                new Position(0, 0), new Position(2, 1), new Position(4, 2), new Position(5, 3)));
    assertTrue(board.canPlace(piece));

    Board.PlaceResult r = board.placeAndClear(piece);
    assertEquals(LineClearType.NONE, r.lineClear());
    // 行消去が無くても配置は反映される
    long occupied =
        r.board().occupancy().stream().flatMap(List::stream).filter(Boolean::booleanValue).count();
    assertEquals(4, occupied);
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
