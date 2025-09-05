package com.example.tetoris.domain.rules;

import static org.junit.jupiter.api.Assertions.*;

import com.example.tetoris.domain.rules.impl.BasicScoreRule;
import com.example.tetoris.domain.value.LineClearType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScoreRuleTest {
  @Test
  @DisplayName("LineClearTypeごとの基本スコアを返す（NONE=0, SINGLE=100, DOUBLE=300, TRIPLE=500, TETRIS=800）")
  void lineClearScore_mapping() {
    ScoreRule rule = new BasicScoreRule();
    assertEquals(0, rule.lineClearScore(LineClearType.NONE));
    assertEquals(100, rule.lineClearScore(LineClearType.SINGLE));
    assertEquals(300, rule.lineClearScore(LineClearType.DOUBLE));
    assertEquals(500, rule.lineClearScore(LineClearType.TRIPLE));
    assertEquals(800, rule.lineClearScore(LineClearType.TETRIS));
  }

  @Test
  @DisplayName("コンボボーナスは50*combo を返す（0→0,1→50,2→100）")
  void comboBonus_linear50PerCombo() {
    ScoreRule rule = new BasicScoreRule();
    assertEquals(0, rule.comboBonus(0));
    assertEquals(50, rule.comboBonus(1));
    assertEquals(100, rule.comboBonus(2));
    assertEquals(150, rule.comboBonus(3));
  }

  @Test
  @DisplayName("コンボボーナス: 負の値は0（ガード分岐）")
  void comboBonus_negative_returns_zero() {
    ScoreRule rule = new BasicScoreRule();
    assertEquals(0, rule.comboBonus(-1));
    assertEquals(0, rule.comboBonus(-100));
  }
}
