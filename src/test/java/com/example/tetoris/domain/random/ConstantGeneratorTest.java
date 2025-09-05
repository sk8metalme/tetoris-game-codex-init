package com.example.tetoris.domain.random;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.random.impl.ConstantGenerator;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConstantGeneratorTest {

  @Test
  @DisplayName("next/preview/reseed: 常に同じミノを返す")
  void constant_behavior() {
    ConstantGenerator g = new ConstantGenerator(TetrominoType.T);
    assertEquals(TetrominoType.T, g.next());
    assertEquals(List.of(TetrominoType.T, TetrominoType.T, TetrominoType.T), g.preview(3));
    TetrominoGenerator g2 = g.reseed(999L);
    assertEquals(TetrominoType.T, g2.next());
  }
}
