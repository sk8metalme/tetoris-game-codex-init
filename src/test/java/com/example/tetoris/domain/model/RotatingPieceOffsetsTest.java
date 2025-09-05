package com.example.tetoris.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.model.impl.RotatingPiece;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RotatingPieceOffsetsTest {

  @Test
  @DisplayName("全ミノ×全向きのcells()が4セルを返す（重複なし）")
  void all_types_all_rotations_have_four_cells() {
    for (TetrominoType t : TetrominoType.values()) {
      for (Rotation r : Rotation.values()) {
        RotatingPiece p = new RotatingPiece(t, r, 10, 10);
        assertEquals(4, p.cells().size(), t+"/"+r+" should have 4 cells");
        Set<String> uniq = new HashSet<>();
        p.cells().forEach(pos -> uniq.add(pos.x()+","+pos.y()));
        assertEquals(4, uniq.size(), t+"/"+r+" cells should be unique");
      }
    }
  }
}

