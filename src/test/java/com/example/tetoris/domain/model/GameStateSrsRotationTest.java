package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.GridBoard;
import com.example.tetoris.domain.model.impl.RotatingPiece;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.Size;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameStateSrsRotationTest {

  private static Set<Position> setOf(Position... ps) {
    return new HashSet<>(java.util.Arrays.asList(ps));
  }

  @Test
  @DisplayName("Tミノ: 空間があればCW回転が適用される（kick 0,0）")
  void rotateCW_T_applies_without_kick() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);
    RotatingPiece t = new RotatingPiece(TetrominoType.T, Rotation.R0, 3, 0);
    GameState s = GameState.of(board, t);

    GameState r = s.rotateCW();

    // R90 の期待セル（アンカー(3,0) 基準の定義に従う）
    Set<Position> expected =
        setOf(new Position(4, 0), new Position(4, 1), new Position(5, 1), new Position(4, 2));
    assertEquals(expected, new HashSet<>(r.current().cells()));
  }

  @Test
  @DisplayName("Tミノ: CCW回転（R0→R270）が適用される")
  void rotateCCW_T_applies() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);
    RotatingPiece t = new RotatingPiece(TetrominoType.T, Rotation.R0, 3, 0);
    GameState s = GameState.of(board, t);

    GameState r = s.rotateCCW();
    // R270 期待
    Set<Position> expected =
        setOf(new Position(4, 0), new Position(3, 1), new Position(4, 1), new Position(4, 2));
    assertEquals(expected, new HashSet<>(r.current().cells()));
  }

  @Test
  @DisplayName("kick全失敗なら回転は不変（R0→R90）")
  void rotate_cw_all_kicks_fail_keeps_state() {
    Size size = Size.of(10, 20);
    boolean[][] g = new boolean[size.height()][size.width()];
    // アンカー(3,0), R0 → R90 の候補をすべて塞ぐ：
    // 0,0: R90 基本形 { (4,0),(4,1),(5,1),(4,2) } のうち (4,0),(4,1),(4,2),(5,1) を塞ぐ
    g[0][4] = true;
    g[1][4] = true;
    g[2][4] = true;
    g[1][5] = true;
    // (-1,0) kick での位置 { (3,0),(3,1),(4,1),(3,2) } も塞ぐ
    g[0][3] = true;
    g[2][3] = true;
    // (-1,1) はさらに y+1 側も概ね塞ぐ（(3,3)等）
    g[3][3] = true;
    // (0,-2) / (-1,-2) は y<0 になり不可能（盤外）

    Board board = GridBoard.fromBooleans(size, g);
    RotatingPiece t = new RotatingPiece(TetrominoType.T, Rotation.R0, 3, 0);
    GameState s = GameState.of(board, t);
    GameState r = s.rotateCW();
    assertEquals(new HashSet<>(t.cells()), new HashSet<>(r.current().cells()));
  }

  @Test
  @DisplayName("回転未対応のPieceはrotateCCWでも不変")
  void rotateCCW_nonRotatingPiece_noop() {
    Size size = Size.of(10, 20);
    Board board = GridBoard.empty(size);
    Piece fixed = new Piece() {
      @Override
      public java.util.List<Position> cells() {
        return java.util.List.of(new Position(0, 0));
      }
    };
    GameState s = GameState.of(board, fixed);
    GameState r = s.rotateCCW();
    assertEquals(new HashSet<>(fixed.cells()), new HashSet<>(r.current().cells()));
  }

  @Test
  @DisplayName("回転先に衝突があってもkickで回避できれば回転が適用される")
  void rotate_blocked_by_occupancy() {
    Size size = Size.of(10, 20);
    boolean[][] g = new boolean[size.height()][size.width()];
    // 回転後（R90）の(5,1) にブロックを置いて衝突させる
    g[1][5] = true;
    Board board = GridBoard.fromBooleans(size, g);
    RotatingPiece t = new RotatingPiece(TetrominoType.T, Rotation.R0, 3, 0);
    GameState s = GameState.of(board, t);

    GameState r = s.rotateCW();
    // SRS kick (-1,0) が適用され、R90の形で左へシフトして回転成功
    Set<Position> expected =
        setOf(new Position(3, 0), new Position(3, 1), new Position(4, 1), new Position(3, 2));
    assertEquals(expected, new HashSet<>(r.current().cells()));
  }
}
