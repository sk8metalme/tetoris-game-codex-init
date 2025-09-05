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

class GameStateRotateTest {

  @Test
  @DisplayName("Oピース: rotateCWしてもセルは変化しない（MVP段階）")
  void rotateCW_keepsOShape() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);

    Piece o = oBlockAt(5, 0);
    GameState s = GameState.of(board, o);

    GameState r = s.rotateCW();

    assertEquals(asSet(o.cells()), asSet(r.current().cells()), "Oは回転しても同一形状");
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

  private static Set<Position> asSet(List<Position> cells) {
    return new HashSet<>(cells);
  }
}
