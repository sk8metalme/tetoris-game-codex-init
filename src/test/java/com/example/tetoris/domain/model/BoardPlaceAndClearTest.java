package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.value.LineClearType;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardPlaceAndClearTest {

  @Test
  @DisplayName("1行未完成の下段に4セルを追加するとSINGLEが発生する（予定）")
  void placeAndClear_singleLine() {
    Size size = Size.of(10, 4);
    boolean[][] g = new boolean[size.height()][size.width()];
    // 底行（y=3）に6セル埋めておく: x=0..5
    for (int x = 0; x < 6; x++) {
      g[3][x] = true;
    }
    Board board = GridBoard.fromBooleans(size, g);

    // 4セル（x=6..9, y=3）を追加すると1行完成する想定
    Piece piece =
        new FakePiece(
            List.of(
                new Position(6, 3), new Position(7, 3), new Position(8, 3), new Position(9, 3)));

    assertTrue(board.canPlace(piece));
    Board.PlaceResult result = board.placeAndClear(piece);

    assertEquals(LineClearType.SINGLE, result.lineClear(), "1行消去のはず");
  }

  // テスト専用の簡易ピース（絶対座標のセル集合）
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
