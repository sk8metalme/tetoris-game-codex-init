package com.example.tetoris.domain.rules;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.rules.srs.SrsRotationSystem;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.Size;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SrsKicksTableTest {

  private final RotationSystem srs = new SrsRotationSystem();

  @Test
  @DisplayName("spawnPosition: 中央寄せ（幅10→x=3）")
  void spawnPosition_centered() {
    assertEquals(new Position(3, 0), srs.spawnPosition(Size.of(10, 20), TetrominoType.T));
  }

  @Test
  @DisplayName("JLTSZ: R0→R90 のkick順序（Tミノ）")
  void jltsz_kicks_R0_to_R90() {
    List<Position> k = srs.kicks(TetrominoType.T, Rotation.R0, Rotation.R90);
    assertEquals(5, k.size());
    assertEquals(new Position(0, 0), k.get(0));
    assertEquals(new Position(-1, 0), k.get(1));
    assertEquals(new Position(-1, 1), k.get(2));
    assertEquals(new Position(0, -2), k.get(3));
    assertEquals(new Position(-1, -2), k.get(4));
  }

  @Test
  @DisplayName("JLTSZ: R90→R0 のkick順序（Tミノ, CCW）")
  void jltsz_kicks_R90_to_R0_ccw() {
    List<Position> k = srs.kicks(TetrominoType.T, Rotation.R90, Rotation.R0);
    assertEquals(5, k.size());
    assertEquals(new Position(0, 0), k.get(0));
    assertEquals(new Position(-1, 0), k.get(1));
    assertEquals(new Position(-1, 1), k.get(2));
    assertEquals(new Position(0, -2), k.get(3));
    assertEquals(new Position(-1, -2), k.get(4));
  }

  @Test
  @DisplayName("I: R90→R0 CCW のkick順序")
  void i_kicks_R90_to_R0_ccw() {
    List<Position> k = srs.kicks(TetrominoType.I, Rotation.R90, Rotation.R0);
    assertEquals(5, k.size());
    assertEquals(new Position(0, 0), k.get(0));
    assertEquals(new Position(2, 0), k.get(1));
    assertEquals(new Position(-1, 0), k.get(2));
    assertEquals(new Position(2, -1), k.get(3));
    assertEquals(new Position(-1, 2), k.get(4));
  }
}
