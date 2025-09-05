package com.example.tetoris.domain.rules;

import com.example.tetoris.domain.value.LineClearType;

public interface ScoreRule {
  int lineClearScore(LineClearType type);

  default int comboBonus(int combo) {
    return 0;
  }
}
