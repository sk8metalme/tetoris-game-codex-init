package com.example.tetoris.domain.rules.srs;

import com.example.tetoris.domain.rules.RotationSystem;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.Size;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.ArrayList;
import java.util.List;

/** SRS（簡易版）: 最小限のkick表を実装。 */
public class SrsRotationSystem implements RotationSystem {
  @Override
  public Position spawnPosition(Size boardSize, TetrominoType type) {
    // 4列幅を基準に中央寄せスポーン（簡易）。
    int x = Math.max(0, boardSize.width() / 2 - 2);
    return new Position(x, 0);
  }

  @Override
  public List<Position> kicks(TetrominoType type, Rotation from, Rotation to) {
    // 最低限、(0,0) を含む。
    List<Position> list = new ArrayList<>();
    list.add(new Position(0, 0));

    if (type == TetrominoType.O) {
      return List.copyOf(list);
    }

    boolean cw = to == rotateCW(from);
    boolean ccw = to == rotateCCW(from);

    if (type == TetrominoType.I) {
      // Iミノは専用kick表（簡易）。
      if (cw) {
        switch (from) {
          case R0 -> addAll(list, new int[][] {{-2, 0}, {1, 0}, {-2, -1}, {1, 2}});
          case R90 -> addAll(list, new int[][] {{-1, 0}, {2, 0}, {-1, 2}, {2, -1}});
          case R180 -> addAll(list, new int[][] {{1, 0}, {-2, 0}, {1, -2}, {-2, 1}});
          case R270 -> addAll(list, new int[][] {{2, 0}, {-1, 0}, {2, -1}, {-1, 2}});
        }
      } else if (ccw) {
        switch (from) {
          case R0 -> addAll(list, new int[][] {{-1, 0}, {2, 0}, {-1, 2}, {2, -1}});
          case R90 -> addAll(list, new int[][] {{2, 0}, {-1, 0}, {2, -1}, {-1, 2}});
          case R180 -> addAll(list, new int[][] {{1, 0}, {-2, 0}, {1, -2}, {-2, 1}});
          case R270 -> addAll(list, new int[][] {{-2, 0}, {1, 0}, {-2, -1}, {1, 2}});
        }
      }
      return List.copyOf(list);
    }

    // JLTSZ 共通kick（簡易）。
    if (cw) {
      switch (from) {
        case R0 -> addAll(list, new int[][] {{-1, 0}, {-1, 1}, {0, -2}, {-1, -2}});
        case R90 -> addAll(list, new int[][] {{1, 0}, {1, -1}, {0, 2}, {1, 2}});
        case R180 -> addAll(list, new int[][] {{1, 0}, {1, 1}, {0, -2}, {1, -2}});
        case R270 -> addAll(list, new int[][] {{-1, 0}, {-1, -1}, {0, 2}, {-1, 2}});
      }
    } else if (ccw) {
      switch (from) {
        case R0 -> addAll(list, new int[][] {{1, 0}, {1, 1}, {0, -2}, {1, -2}});
        case R90 -> addAll(list, new int[][] {{-1, 0}, {-1, 1}, {0, -2}, {-1, -2}});
        case R180 -> addAll(list, new int[][] {{-1, 0}, {-1, -1}, {0, 2}, {-1, 2}});
        case R270 -> addAll(list, new int[][] {{1, 0}, {1, -1}, {0, 2}, {1, 2}});
      }
    }

    return List.copyOf(list);
  }

  private static Rotation rotateCW(Rotation r) {
    return switch (r) {
      case R0 -> Rotation.R90;
      case R90 -> Rotation.R180;
      case R180 -> Rotation.R270;
      case R270 -> Rotation.R0;
    };
  }

  private static Rotation rotateCCW(Rotation r) {
    return switch (r) {
      case R0 -> Rotation.R270;
      case R90 -> Rotation.R0;
      case R180 -> Rotation.R90;
      case R270 -> Rotation.R180;
    };
  }

  private static void addAll(List<Position> list, int[][] deltas) {
    for (int[] d : deltas) {
      list.add(new Position(d[0], d[1]));
    }
  }
}
