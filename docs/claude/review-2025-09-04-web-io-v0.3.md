## レビュー依頼: Web入出力 詳細化 v0.3（Idempotency/DTO拡張）

### 変更点
- Idempotency-Key（任意）導入：
  - POST `/api/game/start` は `Idempotency-Key` 同一で同じ `id` を返却
  - POST `/api/game/{id}/input` は `Idempotency-Key` 同一で同じ結果（rev/state）を返却
- DTO拡張：
  - `GameStateDto` に `width`/`height` を追加

### 相談事項
1. start の Idempotency-Key 運用（キー→idの保持期間・掃除方針）
2. input の Idempotency-Key はセッション単位で保持（現在はin-memory Map）。将来の永続化層での扱い指針
3. DTOの幅/高さ追加タイミングは妥当か。他に最小で入れるべき項目（例: timestamp）

問題なければ v0.4 で `current.type` や `board` の型マッピング拡張を検討します。コメントお願いします。
