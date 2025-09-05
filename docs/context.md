# コンテキスト管理

## 進捗ログ（UTC-8想定）
- 2025-09-04: ステップ1「PJ計画」完了。
- 2025-09-04: ステップ2「PJ内部レビュー/改善」完了。`docs/plan.md` を v0.2 に更新し、次アクション（詳細設計）を定義。
- 2025-09-04: ステップ3-1「詳細設計: ドメイン公開APIドラフト v0.1」作成。`docs/design.md` を追加。
- 2025-09-04: ステップ3-2「詳細設計: ドメイン責務/相互作用整理」完了。`docs/design.md` に責務境界/代表フロー/テスト方針を追記。
- 2025-09-04: ステップ3-3「詳細設計: 難易度・ステージパラメータ定義 v0.1」完了。`docs/design.md` にパラメータ表と計算規則を追記。

## 現在の状態
- 2025-09-04: ステップ3-4「詳細設計: Web APIエンドポイント案作成 v0.1」完了。`docs/design.md` にAPI一覧/DTO/エラー方針を追記。

## 現在の状態
- 2025-09-04: ステップ4「詳細設計内部レビュー/改善 v0.1」完了。`docs/design.md` にレビュー結果/決定事項/チェックリストを追記。

## 現在の状態
- 2025-09-04: ステップ5「テスト設計ドラフト v0.1」完了。`docs/test-plan.md` を追加。

## 現在の状態
- 2025-09-04: ステップ6「テスト設計内部レビュー/改善 v0.2」完了。`docs/test-plan.md` を更新。

## 現在の状態
- 2025-09-04: ステップ7-1「TDD: 初期設定と最初の失敗テスト」完了。Gradle初期化、Spotless/JaCoCo設定、`SrsRotationSystemTest`（失敗想定）を追加。

## 現在の状態
- 2025-09-04: ステップ7-2「TDD: SRSのkick表（最小）実装」完了。`SrsRotationSystem#kicks` を実装（全ピースに(0,0)を含む、T/J/L/S/ZのCW/CCW最小セット、I/O特例）。

## 現在の状態
- 2025-09-04: ステップ7-3-1「TDD: Board.placeAndClear 赤テスト追加」完了。`BoardPlaceAndClearTest` を追加し、`GridBoard` 最小実装（未実装ロジック）で失敗を確認予定。

## 現在の状態
- 2025-09-04: ステップ7-3-2「TDD: Board.placeAndClear 実装（緑化）」完了。`GridBoard` の `isVacant`/`canPlace`/`placeAndClear` を実装し、SINGLE判定をサポート。

## 現在の状態
- 2025-09-04: ステップ7-4-1「TDD: ScoreRule 赤テスト追加」完了。`BasicScoreRule`（スタブ）と `ScoreRuleTest` を追加（期待値: NONE=0, 100/300/500/800, combo=50*combo）。

## 現在の状態
- 2025-09-04: ステップ7-4-2「TDD: ScoreRule 実装（緑化）」完了。`BasicScoreRule` に基本スコア（0/100/300/500/800）と `comboBonus=50*combo` を実装。

## 現在の状態
- 2025-09-04: ステップ7-5-1「TDD: GameState 基本遷移 赤テスト追加」完了。`GameState` スケルトンと `GameStateTest`（moveLeftの期待）を追加。
- 2025-09-04: ステップ7-5-2「TDD: GameState.moveLeft 実装（緑化）」完了。`ShiftedPiece` により左移動を実装し、テスト緑化。

## 現在の状態
- 2025-09-04: ステップ7-5-3-1「TDD: GameState.moveRight 赤テスト追加」完了。`GameStateTest` に moveRight の期待を追記。
- 2025-09-04: ステップ7-5-3-2「TDD: GameState.moveRight 実装（緑化）」完了。右移動を実装し、テスト緑化（6/0/0）。

## 現在の状態
- 2025-09-04: ステップ7-5-4-1「TDD: GameState.softDrop 赤テスト追加」完了。`GameStateTest` に softDrop の期待を追記。
- 2025-09-04: ステップ7-5-4-2「TDD: GameState.softDrop 実装（緑化）」完了。1マス落下を実装し、テスト緑化（7/0/0）。

## 現在の状態
- 2025-09-04: ステップ7-5-5-1「TDD: GameState.hardDrop 赤テスト追加」完了。`GameStateTest` に hardDrop（空盤面で底まで）の期待を追記。
- 2025-09-04: ステップ7-5-5-2「TDD: GameState.hardDrop 実装（緑化）」完了。連続落下を実装し、テスト緑化（8/0/0）。

## 現在の状態
- 2025-09-04: ステップ7-6-1「TDD: TetrominoGenerator 赤テスト追加」完了。`TetrominoGeneratorTest` を追加（7-bagの2バッグ性、seed再現性）。最小スタブ（ConstantGenerator）と`Generators.sevenBag`を仮実装。

## 現在の状態
- 2025-09-04: Claude相談リクエストを作成。`docs/claude/consult-2025-09-04-7bag.md` に論点・実装案・質問を整理。

## 現在の状態
- 2025-09-04: ステップ7-6-2「TDD: TetrominoGenerator 実装（7-bag緑化）」完了。`SevenBagGenerator` 実装と `Generators.sevenBag` 差し替え。テスト緑化（10/0/0）。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-7bag-impl.md` を追加。

## 現在の状態
- 2025-09-04: ステップ8-1「ドキュメント最新化: README 更新 v0.1」完了。`README.md` を刷新。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-docs-readme.md` を追加。

## 次ステップ
- 2025-09-04: ステップ8-2-1「ドキュメント最新化: 設計の同期」完了。`docs/design.md` に実装状況を追記。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-docs-design-sync.md` を追加。

## 次ステップ
- 2025-09-04: ステップ8-2-2「ドキュメント最新化: テスト計画の同期（test-plan）」完了。`docs/test-plan.md` をv0.3へ更新。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-docs-test-sync.md` を追加。

## 現在の状態
- 2025-09-04: ステップ7-7「Web/APIスケルトン作成 v0.1」完了。Controller/DTO/Service の骨組みを追加し、ビルド成功。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-web-skeleton.md` を追加。

## 次ステップ
- 2025-09-04: ステップ7-7-1「Web入出力 詳細化 v0.2」完了。If-Match/ETag（rev）対応、404/409/400エラー整備。ビルド成功。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-web-io-v0.2.md` を追加。

## 現在の状態
- 2025-09-04: ステップ7-7-2「Web入出力 詳細化 v0.3」完了。Idempotency-Key（start/input）とDTO拡張（width/height）を実装。ビルド成功。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-web-io-v0.3.md` を追加。
## 現在の状態
- 2025-09-04: ステップ7-7-3「Web入出力 詳細化 v0.4」完了。DTOに `cellTypes` を追加し、型マッピングの下地を用意。ビルド成功。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-web-io-v0.4.md` を追加。


## 次ステップ
- ステップ9「全体の内部レビュー」へ移行、または Web入出力 v0.4（DTO型マッピング拡張）に着手。

## 決定事項（要点）
- オニオンアーキテクチャ採用。単一Gradleモジュールでパッケージ分割から開始。
- ドメインはJavaで実装し高カバレッジを確保。WebはThymeleaf + JSで描画。
- コマンド方針: `gradle build` / `gradle test` / `gradle lint` を整備。

## 現在の状態
- 2025-09-04: ステップ7-7-4「Web入出力 v0.5（簡易UI）」完了。Thymeleafページ/静的アセット/ページコントローラを追加し、操作・描画を確認。ビルド成功。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-web-ui-v0.5.md` を追加。

## 現在の状態
- 2025-09-04: ステップ7-7-5「Web入出力 v0.7（UI色マッピング強化）」完了。cellTypesに基づくクラス付与と色定義を追加。ビルド成功。
- 2025-09-04: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-04-web-ui-v0.7.md` を追加。

## 現在の状態
- 2025-09-05: ステップ8-3「E2E 手動手順（ブラウザ）追加 v0.1」完了。`docs/e2e-manual.md` を追加。
- 2025-09-05: Claudeレビュー依頼を作成。`docs/claude/review-2025-09-05-e2e-manual.md` を追加。

## 現在の状態
- 2025-09-05: ステップ9「全体の内部レビュー」完了。`docs/review/internal-2025-09-05.md` を追加。
- 2025-09-05: ステップ10「各種エージェントへの改善指示」完了。`docs/improvements/todo-2025-09-05.md` を追加。
