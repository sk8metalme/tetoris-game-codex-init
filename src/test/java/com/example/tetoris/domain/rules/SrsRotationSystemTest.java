package com.example.tetoris.domain.rules;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.rules.srs.SrsRotationSystem;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SrsRotationSystemTest {

  @Test
  @DisplayName("SRS: Tミノ R0→R90 のkick候補には (0,0) を含み、空ではない")
  void kicks_shouldContainOrigin_for_T_piece_R0_to_R90() {
    RotationSystem srs = new SrsRotationSystem();

    List<Position> kicks = srs.kicks(TetrominoType.T, Rotation.R0, Rotation.R90);

    assertNotNull(kicks, "kicksはnullであってはならない");
    assertFalse(kicks.isEmpty(), "kicksは空であってはならない");
    assertTrue(kicks.contains(new Position(0, 0)), "(0,0) がkick候補に含まれるべき");
  }
}
