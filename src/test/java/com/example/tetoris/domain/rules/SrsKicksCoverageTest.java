package com.example.tetoris.domain.rules;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.rules.srs.SrsRotationSystem;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SrsKicksCoverageTest {

  @Test
  @DisplayName("Iミノ: 各向きのCW/CCWで最低5要素((0,0)+4候補)")
  void i_piece_all_rotations_have_kicks() {
    Rotation[] rs = {Rotation.R0, Rotation.R90, Rotation.R180, Rotation.R270};
    SrsRotationSystem srs = new SrsRotationSystem();
    for (Rotation from : rs) {
      Rotation toCw = nextCW(from);
      Rotation toCcw = nextCCW(from);
      List<Position> cw = srs.kicks(TetrominoType.I, from, toCw);
      List<Position> ccw = srs.kicks(TetrominoType.I, from, toCcw);
      assertTrue(cw.size() >= 5 && ccw.size() >= 5, "I kicks should be >=5 including origin");
      assertTrue(cw.contains(new Position(0, 0)) && ccw.contains(new Position(0, 0)));
    }
  }

  @Test
  @DisplayName("JLTSZ: R180→R270(CW) と R90→R0(CCW) など代表ケース")
  void jltsz_some_cases() {
    SrsRotationSystem srs = new SrsRotationSystem();
    List<Position> cw = srs.kicks(TetrominoType.T, Rotation.R180, Rotation.R270);
    List<Position> ccw = srs.kicks(TetrominoType.J, Rotation.R90, Rotation.R0);
    assertTrue(cw.size() >= 5 && ccw.size() >= 5);
    assertTrue(cw.contains(new Position(0, 0)) && ccw.contains(new Position(0, 0)));
  }

  private static Rotation nextCW(Rotation r) {
    return switch (r) {
      case R0 -> Rotation.R90;
      case R90 -> Rotation.R180;
      case R180 -> Rotation.R270;
      case R270 -> Rotation.R0;
    };
  }

  private static Rotation nextCCW(Rotation r) {
    return switch (r) {
      case R0 -> Rotation.R270;
      case R90 -> Rotation.R0;
      case R180 -> Rotation.R90;
      case R270 -> Rotation.R180;
    };
  }
}

