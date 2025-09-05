package com.example.tetoris.domain.random.impl;

import com.example.tetoris.domain.random.TetrominoGenerator;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.ArrayList;
import java.util.List;

/** 常に同じミノを返すスタブ実装（赤テスト用に失敗させる目的）。 */
public class ConstantGenerator implements TetrominoGenerator {
  private final TetrominoType type;

  public ConstantGenerator(TetrominoType type) {
    this.type = type;
  }

  @Override
  public TetrominoType next() {
    return type;
  }

  @Override
  public List<TetrominoType> preview(int count) {
    List<TetrominoType> list = new ArrayList<>(count);
    for (int i = 0; i < count; i++) list.add(type);
    return list;
  }

  @Override
  public TetrominoGenerator reseed(long seed) {
    return new ConstantGenerator(type);
  }
}
