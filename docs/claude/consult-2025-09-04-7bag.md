# 相談トピック: TetrominoGenerator（7-bag）設計/実装レビュー依頼

## 背景（要約）
- 技術: Java 21 / オニオンアーキテクチャ / TDD。
- 現状: `TetrominoGeneratorTest` を追加し、7-bagの2バッグ性（先頭14で各7種1回ずつ）とseed再現性を要求。いまは `Generators.sevenBag(seed)` がスタブのためテストは失敗中（赤）。
- 目的: 7-bagの実装方針合意と、API（`preview`/`reseed`）の細部確認。

## 現在のIF（domain.random）
```java
public interface TetrominoGenerator {
  TetrominoType next();
  List<TetrominoType> preview(int count);
  TetrominoGenerator reseed(long seed);
}

public final class Generators {
  public static TetrominoGenerator sevenBag(long seed) { /* TODO */ }
}
```

## 実装案（提案）
- PRNG: `java.util.random.RandomGenerator` 実装のうち `SplittableRandom` ではなく `java.util.Random` もしくは `Xoroshiro128PlusPlus`（JDK21）を採用。理由: 再現性と実装容易性。MVPでは`Random`で十分。
- 7-bag生成: `TetrominoType[] BAG = {I,O,T,S,Z,J,L}` をFisher–Yatesでシャッフル。キューに追加。枯渇時は新しいバッグを同様に生成して連結。
- `next()`: 先頭をpopし、必要なら新バッグを補充。
- `preview(n)`: 先頭からn要素をスナップショット（nが残量超過時は新バッグを仮想的に補って返却。ただし内部状態は進めない）。
- `reseed(seed)`: 内部RNGをseedで初期化した新インスタンスを返却。
- スレッド安全: 不要（単一ゲームスレッド想定）。

## テスト対応
- 2バッグ性: 先頭14件の集合サイズが各7であることを検証。
- 再現性: 同一seedで20件の列が一致、別seedでは高確率で不一致（現在のテストは`assertNotEquals`）。

## 論点と質問
1. RNGの選定: `Random`で十分か、`Xoroshiro128PlusPlus`等にすべきか（将来の安定性/互換性）。
2. `preview(n)`の仕様: 残量不足時に「内部状態を進めず」未来バッグを見せる実装でよいか（MVPではOK想定）。
3. 連続バッグ境界: 同種がバッグ跨ぎで連続するのは仕様上OK（一般的挙動）。明示確認。
4. 例外/制約: `n<0` など不正引数は`IllegalArgumentException`でよいか。

## 実装スケッチ（案）
```java
public final class SevenBagGenerator implements TetrominoGenerator {
  private final Random rng; // new Random(seed)
  private final Deque<TetrominoType> queue = new ArrayDeque<>();

  public SevenBagGenerator(long seed) { this.rng = new Random(seed); refill(); }

  private void refill() {
    TetrominoType[] bag = TetrominoType.values(); // {I,O,T,S,Z,J,L} の順に定義しておく
    // Fisher–Yates
    for (int i = bag.length - 1; i > 0; i--) {
      int j = rng.nextInt(i + 1);
      var tmp = bag[i]; bag[i] = bag[j]; bag[j] = tmp;
    }
    Collections.addAll(queue, bag);
  }

  public TetrominoType next() { if (queue.isEmpty()) refill(); return queue.removeFirst(); }

  public List<TetrominoType> preview(int n) {
    if (n < 0) throw new IllegalArgumentException("n must be >= 0");
    while (queue.size() < n) refill();
    return queue.stream().limit(n).toList();
  }

  public TetrominoGenerator reseed(long seed) { return new SevenBagGenerator(seed); }
}
```

## 依頼
- 上記案の是非（RNG選定/`preview`挙動/例外方針/境界ケース）をご確認ください。
- 問題なければ実装に着手し、`Generators.sevenBag(seed)`で公開します。

---
（Codex作成: 2025-09-04, 7-bag緑化前の相談）

