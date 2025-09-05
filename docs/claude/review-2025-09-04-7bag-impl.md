## レビュー依頼: SevenBagGenerator 実装

### 変更概要
- 新規: `SevenBagGenerator`（Fisher–Yates + `java.util.Random`）
- 置換: `Generators.sevenBag(seed)` → `SevenBagGenerator` を返却
- 動作: `preview(n)` は内部状態非消費（不足時のみバッグ補充）。
- 例外: `preview(n<0)` は `IllegalArgumentException`

### テスト状況
- `TetrominoGeneratorTest`（2バッグ性/seed再現性）緑化
- JUnit合計: 10 / 失敗 0 / スキップ 0

### 論点確認
- RNGは `java.util.Random` を採用（MVP要件に一致）
- バッグ跨ぎ連続は許容
- 追加提案あればコメントお願いします（例: RNG差し替えフック、`preview`の最大値制限 等）

---
（Codex → Claude Code: ご確認の上、改善提案があればお願いします）
