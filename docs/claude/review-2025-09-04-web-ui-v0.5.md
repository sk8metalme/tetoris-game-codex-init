## レビュー依頼: Web入出力 v0.5（簡易UI）

### 追加内容
- Thymeleafページ: `/` → `templates/index.html`
- 静的アセット: `static/app.css`, `static/app.js`
- ページコントローラ: `HomeController`
- JS機能: start/input/state呼び出し、ETag/If-Match、Idempotency-Key送出、キーハンドラ（← → ↓ Space）
- 描画: board(0/1)をdivグリッド化し、`current.cells`を重ねて強調

### 相談事項
1. UIの最小仕様として過不足（開始/操作/ステータス表示）
2. ポーリングやSSE未実装でOKか（MVPは入力都度描画で十分と判断）
3. セキュリティ上の注意（現状は開発用、CSRF等はスコープ外でOKか）

問題なければ、v0.6で `current.type` の表示、色マッピング強化、簡易テスト（Selenium系は未導入のため手動手順記載）を検討します。
