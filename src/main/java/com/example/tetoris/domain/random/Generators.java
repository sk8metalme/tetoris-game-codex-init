package com.example.tetoris.domain.random;

public final class Generators {
  private Generators() {}

  /** 7-bagジェネレータを返すファクトリ（現時点はスタブ実装を返却）。 後続ステップで正規の7-bag実装に差し替える。 */
  public static TetrominoGenerator sevenBag(long seed) {
    return new com.example.tetoris.domain.random.impl.SevenBagGenerator(seed);
  }
}
