package com.example.tetoris.domain.model.impl;

import com.example.tetoris.domain.model.Piece;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.ArrayList;
import java.util.List;

/**
 * 回転可能なテトロミノ（簡易SRS想定）。
 * アンカー座標 (ax, ay) を基準とした相対オフセット定義を各向きごとに持つ。
 */
public final class RotatingPiece implements Piece {
  private final TetrominoType type;
  private final Rotation rotation;
  private final int ax;
  private final int ay;

  public RotatingPiece(TetrominoType type, Rotation rotation, int anchorX, int anchorY) {
    this.type = type;
    this.rotation = rotation;
    this.ax = anchorX;
    this.ay = anchorY;
  }

  public TetrominoType type() {
    return type;
  }

  public Rotation rotation() {
    return rotation;
  }

  public int anchorX() {
    return ax;
  }

  public int anchorY() {
    return ay;
  }

  public RotatingPiece withRotation(Rotation r) {
    return new RotatingPiece(type, r, ax, ay);
  }

  public RotatingPiece withAnchor(int x, int y) {
    return new RotatingPiece(type, rotation, x, y);
  }

  @Override
  public List<Position> cells() {
    int[][] offs = offsets(type, rotation);
    List<Position> out = new ArrayList<>(4);
    for (int[] o : offs) out.add(new Position(ax + o[0], ay + o[1]));
    return out;
  }

  private static int[][] offsets(TetrominoType t, Rotation r) {
    // 定義は 3x3（I/Oは4x4相当）ボックスの左上をアンカーとする簡易版
    // 各配列要素は {dx, dy}
    return switch (t) {
      case O -> new int[][] {{0, 0}, {1, 0}, {0, 1}, {1, 1}}; // どの向きでも同じ
      case I -> switch (r) {
            case R0 -> new int[][] {{0, 1}, {1, 1}, {2, 1}, {3, 1}}; // 横 4 連
            case R90 -> new int[][] {{2, 0}, {2, 1}, {2, 2}, {2, 3}}; // 縦
            case R180 -> new int[][] {{0, 2}, {1, 2}, {2, 2}, {3, 2}};
            case R270 -> new int[][] {{1, 0}, {1, 1}, {1, 2}, {1, 3}};
          };
      case T -> switch (r) {
            case R0 -> new int[][] {{1, 0}, {0, 1}, {1, 1}, {2, 1}};
            case R90 -> new int[][] {{1, 0}, {1, 1}, {2, 1}, {1, 2}};
            case R180 -> new int[][] {{0, 1}, {1, 1}, {2, 1}, {1, 2}};
            case R270 -> new int[][] {{1, 0}, {0, 1}, {1, 1}, {1, 2}};
          };
      case J -> switch (r) {
            case R0 -> new int[][] {{0, 0}, {0, 1}, {1, 1}, {2, 1}};
            case R90 -> new int[][] {{1, 0}, {2, 0}, {1, 1}, {1, 2}};
            case R180 -> new int[][] {{0, 1}, {1, 1}, {2, 1}, {2, 2}};
            case R270 -> new int[][] {{1, 0}, {1, 1}, {0, 2}, {1, 2}};
          };
      case L -> switch (r) {
            case R0 -> new int[][] {{2, 0}, {0, 1}, {1, 1}, {2, 1}};
            case R90 -> new int[][] {{1, 0}, {1, 1}, {1, 2}, {2, 2}};
            case R180 -> new int[][] {{0, 1}, {1, 1}, {2, 1}, {0, 2}};
            case R270 -> new int[][] {{0, 0}, {1, 0}, {1, 1}, {1, 2}};
          };
      case S -> switch (r) {
            case R0 -> new int[][] {{1, 0}, {2, 0}, {0, 1}, {1, 1}};
            case R90 -> new int[][] {{1, 0}, {1, 1}, {2, 1}, {2, 2}};
            case R180 -> new int[][] {{1, 1}, {2, 1}, {0, 2}, {1, 2}};
            case R270 -> new int[][] {{0, 0}, {0, 1}, {1, 1}, {1, 2}};
          };
      case Z -> switch (r) {
            case R0 -> new int[][] {{0, 0}, {1, 0}, {1, 1}, {2, 1}};
            case R90 -> new int[][] {{2, 0}, {1, 1}, {2, 1}, {1, 2}};
            case R180 -> new int[][] {{0, 1}, {1, 1}, {1, 2}, {2, 2}};
            case R270 -> new int[][] {{1, 0}, {0, 1}, {1, 1}, {0, 2}};
          };
    };
  }
}

