package com.example.tetoris.domain.random;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.value.TetrominoType;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TetrominoGeneratorTest {

  @Test
  @DisplayName("7-bag: 先頭14個は2バッグで、各バッグで7種類が1回ずつ出現する")
  void sevenBag_first14_areTwoFullBags() {
    TetrominoGenerator rng = Generators.sevenBag(42L);
    // 先頭7
    Set<TetrominoType> first = new HashSet<>(take(rng, 7));
    // 次の7
    Set<TetrominoType> second = new HashSet<>(take(rng, 7));

    assertEquals(7, first.size(), "最初のバッグは7種であるべき");
    assertEquals(7, second.size(), "次のバッグも7種であるべき");
  }

  @Test
  @DisplayName("seed再現性: 同一seedなら同列、異なるseedなら高確率で異なる")
  void reproducible_and_varies_with_seed() {
    TetrominoGenerator a1 = Generators.sevenBag(123L);
    TetrominoGenerator a2 = Generators.sevenBag(123L);
    TetrominoGenerator b = Generators.sevenBag(456L);

    List<TetrominoType> s1 = take(a1, 20);
    List<TetrominoType> s2 = take(a2, 20);
    List<TetrominoType> s3 = take(b, 20);

    assertEquals(s1, s2, "同一seedは同列のはず");
    assertNotEquals(s1, s3, "異なるseedは列が異なる可能性が高い");
  }

  private static List<TetrominoType> take(TetrominoGenerator g, int n) {
    return g.preview(n);
  }
}
