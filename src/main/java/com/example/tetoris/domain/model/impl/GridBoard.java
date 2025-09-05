package com.example.tetoris.domain.model.impl;

import com.example.tetoris.domain.model.Board;
import com.example.tetoris.domain.model.Piece;
import com.example.tetoris.domain.value.LineClearType;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 盤面の最小不変実装（暫定）。 現段階ではロジック未実装のため、placeAndClearは常にNONEを返す。 */
public final class GridBoard implements Board {
  private final Size size;
  private final boolean[][] grid; // [y][x]

  private GridBoard(Size size, boolean[][] grid) {
    this.size = size;
    this.grid = grid;
  }

  public static GridBoard empty(Size size) {
    return new GridBoard(size, new boolean[size.height()][size.width()]);
  }

  public static GridBoard fromBooleans(Size size, boolean[][] occupancy) {
    return new GridBoard(size, copyOf(occupancy));
  }

  private static boolean[][] copyOf(boolean[][] src) {
    boolean[][] dst = new boolean[src.length][];
    for (int y = 0; y < src.length; y++) {
      dst[y] = new boolean[src[y].length];
      System.arraycopy(src[y], 0, dst[y], 0, src[y].length);
    }
    return dst;
  }

  @Override
  public Size size() {
    return size;
  }

  @Override
  public boolean isInside(Position p) {
    return p.x() >= 0 && p.y() >= 0 && p.x() < size.width() && p.y() < size.height();
  }

  @Override
  public boolean isVacant(Position p) {
    if (!isInside(p)) return false;
    return !grid[p.y()][p.x()];
  }

  @Override
  public boolean canPlace(Piece piece) {
    for (Position c : piece.cells()) {
      if (!isInside(c) || !isVacant(c)) return false;
    }
    return true;
  }

  @Override
  public PlaceResult placeAndClear(Piece piece) {
    // 1) 配置可能性を前提にグリッドを複製
    boolean[][] work = copyOf(this.grid);
    for (Position c : piece.cells()) {
      if (!isInside(c) || work[c.y()][c.x()]) {
        // 配置不能: 副作用なしでNONEを返す
        return new PlaceResult() {
          @Override
          public Board board() {
            return GridBoard.this;
          }

          @Override
          public LineClearType lineClear() {
            return LineClearType.NONE;
          }
        };
      }
      work[c.y()][c.x()] = true;
    }

    // 2) ライン消去の検出と圧縮
    int w = size.width();
    int h = size.height();
    boolean[][] next = new boolean[h][w];
    int writeY = h - 1;
    int cleared = 0;

    for (int y = h - 1; y >= 0; y--) {
      boolean full = true;
      for (int x = 0; x < w; x++) {
        if (!work[y][x]) {
          full = false;
          break;
        }
      }
      if (full) {
        cleared++;
        // skip: この行は消去
      } else {
        // 行をコピーして詰める
        for (int x = 0; x < w; x++) {
          next[writeY][x] = work[y][x];
        }
        writeY--;
      }
    }
    // 残りの上部は空行のまま

    GridBoard after = new GridBoard(size, next);
    LineClearType type = toLineClearType(cleared);
    Board resultBoard = cleared == 0 ? this : after;

    return new PlaceResult() {
      @Override
      public Board board() {
        return resultBoard;
      }

      @Override
      public LineClearType lineClear() {
        return type;
      }
    };
  }

  @Override
  public List<List<Boolean>> occupancy() {
    List<List<Boolean>> out = new ArrayList<>(size.height());
    for (int y = 0; y < size.height(); y++) {
      List<Boolean> row = new ArrayList<>(size.width());
      for (int x = 0; x < size.width(); x++) {
        row.add(grid[y][x]);
      }
      out.add(Collections.unmodifiableList(row));
    }
    return Collections.unmodifiableList(out);
  }

  private static LineClearType toLineClearType(int cleared) {
    return switch (cleared) {
      case 1 -> LineClearType.SINGLE;
      case 2 -> LineClearType.DOUBLE;
      case 3 -> LineClearType.TRIPLE;
      case 4 -> LineClearType.TETRIS;
      default -> LineClearType.NONE;
    };
  }
}
