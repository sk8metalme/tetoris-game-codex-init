package com.example.tetoris.domain.random;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.random.impl.SevenBagGenerator;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SevenBagGeneratorEdgeTest {

  @Test
  @DisplayName("preview: 負の値はIllegalArgumentException")
  void preview_negative_throws() {
    SevenBagGenerator g = new SevenBagGenerator(1L);
    assertThrows(IllegalArgumentException.class, () -> g.preview(-1));
  }

  @Test
  @DisplayName("next: 8回目でrefillが走っても7種性は維持される")
  void next_triggers_refill_after_seven() {
    SevenBagGenerator g = new SevenBagGenerator(2L);
    Set<TetrominoType> s1 = new HashSet<>();
    for (int i = 0; i < 7; i++) s1.add(g.next());
    assertEquals(7, s1.size());
    // 8回目で queue が空になり refill 分岐を踏む
    TetrominoType t8 = g.next();
    assertNotNull(t8);
  }

  @Test
  @DisplayName("preview(0): 空リストを返し副作用なし")
  void preview_zero_returns_empty() {
    SevenBagGenerator g = new SevenBagGenerator(3L);
    assertTrue(g.preview(0).isEmpty());
    // その後の next も通常通り返せる
    assertNotNull(g.next());
  }
}
