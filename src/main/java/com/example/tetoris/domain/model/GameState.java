package com.example.tetoris.domain.model;

import com.example.tetoris.domain.value.Position;
import java.util.ArrayList;
import java.util.List;

/** MVP段階のGameState。必要最小の操作から実装を開始する。 */
public final class GameState {
  private final Board board;
  private final Piece current;

  private GameState(Board board, Piece current) {
    this.board = board;
    this.current = current;
  }

  public static GameState of(Board board, Piece current) {
    return new GameState(board, current);
  }

  public Board board() {
    return board;
  }

  public Piece current() {
    return current;
  }

  public GameState moveLeft() {
    Piece moved = new ShiftedPiece(current, -1, 0);
    if (board.canPlace(moved)) {
      return new GameState(board, moved);
    }
    return this;
  }

  public GameState moveRight() {
    Piece moved = new ShiftedPiece(current, 1, 0);
    if (board.canPlace(moved)) {
      return new GameState(board, moved);
    }
    return this;
  }

  public GameState rotateCW() {
    return this; // 後続ステップで実装
  }

  public GameState rotateCCW() {
    return this; // 後続ステップで実装
  }

  public GameState softDrop() {
    Piece moved = new ShiftedPiece(current, 0, 1);
    if (board.canPlace(moved)) {
      return new GameState(board, moved);
    }
    return this;
  }

  public GameState hardDrop() {
    Piece falling = this.current;
    while (true) {
      Piece next = new ShiftedPiece(falling, 0, 1);
      if (board.canPlace(next)) {
        falling = next;
      } else {
        break;
      }
    }
    return new GameState(board, falling);
  }

  public GameState holdSwap() {
    return this; // 後続ステップで実装
  }

  public GameState tick() {
    return this; // 後続ステップで実装
  }

  private static final class ShiftedPiece implements Piece {
    private final Piece base;
    private final int dx;
    private final int dy;

    private ShiftedPiece(Piece base, int dx, int dy) {
      this.base = base;
      this.dx = dx;
      this.dy = dy;
    }

    @Override
    public List<Position> cells() {
      List<Position> out = new ArrayList<>();
      for (Position p : base.cells()) {
        out.add(new Position(p.x() + dx, p.y() + dy));
      }
      return out;
    }
  }
}
