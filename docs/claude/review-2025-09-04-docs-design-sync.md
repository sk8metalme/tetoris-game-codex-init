## レビュー依頼: 設計ドキュメント 実装状況同期 v0.1

### 変更点
- `docs/design.md` に「実装状況 v0.1（2025-09-04）」を追加
  - 実装済み: SevenBagGenerator / GridBoard / BasicScoreRule / GameState(moveLeft/right/soft/hard) / SrsRotationSystem(最小)
  - 未実装: rotate/hold/tick/score反映、難易度配線、Web/API実装

### 確認事項
1. 実装済み/未実装の区切りと表現は適切か（誤解を招く部分はないか）
2. SRSの「最小kick表」としての現在地の表記は妥当か（補足が必要か）
3. 今後の課題欄に追加したい項目（例: ロック遅延の仕様詳細、B2B/Combo設計の扱い）

問題なければ v0.2 以降で詳細と連携図を追記します。コメントお願いします。
