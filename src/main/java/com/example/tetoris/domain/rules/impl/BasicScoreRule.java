package com.example.tetoris.domain.rules.impl;

import com.example.tetoris.domain.rules.ScoreRule;
import com.example.tetoris.domain.value.LineClearType;

public class BasicScoreRule implements ScoreRule {
  @Override
  public int lineClearScore(LineClearType type) {
    return switch (type) {
      case NONE -> 0;
      case SINGLE -> 100;
      case DOUBLE -> 300;
      case TRIPLE -> 500;
      case TETRIS -> 800;
    };
  }

  @Override
  public int comboBonus(int combo) {
    if (combo <= 0) return 0;
    return 50 * combo;
  }
}
