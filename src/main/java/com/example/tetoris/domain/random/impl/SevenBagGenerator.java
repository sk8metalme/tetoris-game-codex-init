package com.example.tetoris.domain.random.impl;

import com.example.tetoris.domain.random.TetrominoGenerator;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * 7-bag の TetrominoGenerator 実装。 - Fisher–Yates でバッグをシャッフルし、尽きたら補充。 - preview(n)
 * は内部状態を消費せず、必要に応じてバッグ補充のみ行う。
 */
public final class SevenBagGenerator implements TetrominoGenerator {
  private final Random rng;
  private final Deque<TetrominoType> queue = new ArrayDeque<>();

  public SevenBagGenerator(long seed) {
    this.rng = new Random(seed);
    refill();
  }

  @Override
  public TetrominoType next() {
    if (queue.isEmpty()) refill();
    return queue.removeFirst();
  }

  @Override
  public List<TetrominoType> preview(int count) {
    if (count < 0) throw new IllegalArgumentException("count must be >= 0");
    while (queue.size() < count) refill();
    List<TetrominoType> list = new ArrayList<>(count);
    int i = 0;
    for (TetrominoType t : queue) {
      if (i++ >= count) break;
      list.add(t);
    }
    return Collections.unmodifiableList(list);
  }

  @Override
  public TetrominoGenerator reseed(long seed) {
    return new SevenBagGenerator(seed);
  }

  private void refill() {
    TetrominoType[] bag = TetrominoType.values().clone();
    // Fisher–Yates shuffle
    for (int i = bag.length - 1; i > 0; i--) {
      int j = rng.nextInt(i + 1);
      TetrominoType tmp = bag[i];
      bag[i] = bag[j];
      bag[j] = tmp;
    }
    for (TetrominoType t : bag) {
      queue.addLast(t);
    }
  }
}
