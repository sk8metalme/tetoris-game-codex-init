## レビュー依頼: Web入出力 詳細化 v0.2

### 変更点
- If-Match/ETag（rev）導入:
  - GET `/api/game/{id}/state` と POST `/api/game/start`/`/{id}/input` のレスポンスに `ETag: <rev>` を付与
  - POST `/api/game/{id}/input` は `If-Match: <rev>` が付与されている場合に厳密一致を要求（不一致は409 CONFLICT）
- エラーポリシー整備: 404 `GAME_NOT_FOUND`、409 `CONFLICT`、400 `INVALID_REQUEST`

### 相談事項
1. If-Match を任意→必須にするフェーズのタイミング（MVPでは任意のまま運用）
2. ETagの値は素の整数にしています（引用符なし）。将来的に `W/"rev-<n>"` などへ拡張すべきか
3. 未対応アクションの扱い（MVPでは無変化200）を当面維持でOKか

### 後続案
- Idempotency-Keyの導入（重複投稿抑止）
- DTO拡張（boardの色/型マッピング、currentのtype反映）

フィードバックをお願いします。問題なければ v0.3 で Idempotency-Key を検討します。
