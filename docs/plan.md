# PJ計画 v0.2（内部レビュー反映）

## 要約
- 目的: PCブラウザ向けテトリス（シングル、難易度3、ステージ3）。
- 技術: Java 21 / Spring Boot / Thymeleaf、Gradle、JUnit、JaCoCo。
- 方針: オニオンアーキテクチャ（domain → application → infrastructure → presentation）。
- 開発: ワークフローに準拠し、TDDでドメインから実装。
- 品質: Google Java Style、95%+ 文/分岐カバレッジ、`gradle lint`/`gradle test`/`gradle build` を整備。

## スコープ
- 対応: シングルプレイ、基本スコアリング、ホールド/ハードドロップ、ガーベッジ無し、ローカルスコア保存（初期版はメモリ保持）。
- 非対応（MVP外）: マルチプレイ、オンラインランキング、スキン変更、高度なT-Spin判定/ボナスの完全実装（拡張余地は残す）。

## アーキテクチャ
- 層構造: 
  - domain: ボード、テトロミノ、回転/当たり判定、ランダマイザ、難易度/ステージ設定、スコア。
  - application: ユースケース（`startGame`/`tick`/`move`/`rotate`/`softDrop`/`hardDrop`/`hold`）。
  - infrastructure: 設定/永続（初期はメモリ、後日ファイル/DB拡張）。
  - presentation(web): Spring MVC + Thymeleaf、静的JSで描画。APIはREST（JSON）を想定。
- モジュール（単一Gradleモジュール内のパッケージ分割から開始）:
  - `...domain.*` / `...application.*` / `...infrastructure.*` / `...web.*`

## ツールチェーンとコマンド
- `gradle build`: プロダクションJar作成（最適化フラグ、リソース同梱）。
- `gradle test`: JUnit5 + JaCoCo（line/branch 95% しきい値、レポート生成）。
- `gradle lint`: Spotless（google-java-format）+ Checkstyle（Google Java Style）をチェック。

## テスト戦略
- レベル: unit（domain中心）> service（application）> web（最小限のコントローラ単体）。
- 生成物: テスト仕様（docs配下）とカバレッジレポートをCI成果物化。
- TDD: ルール・回転・着地・ライン消去・スコアを優先実装。

## マイルストーン（ワークフロー対応）
1. PJ計画（完了）
2. PJ内部レビュー/改善（本ドキュメントで反映・完了）
3. 詳細設計：
   - ドメインモデル定義（クラス/責務/公開API）。
   - 回転系（SRS簡易版）/衝突/ライン消去/スコアの仕様確定。
   - Web API（/api/game/...）IF草案、画面ラフ（Thymeleaf + JS）
4. 詳細設計内部レビュー/改善
5. テスト設計（ユースケース別テスト項目・カバレッジ計画）
6. テスト設計内部レビュー/改善
7. TDDでの開発（domain→application→webの順）
8. ドキュメント最新化
9-13. レビュー/PR/CI/進捗管理

### ステップ7詳細（TDD計画 v0.1）
- 7-1: ビルド初期化 + 失敗テスト（RotationSystem.kicks）
- 7-2: SRSのkick表（T,JLZS,I,O最小セット）実装 → テスト緑
- 7-3: Board.placeAndClear テスト → 実装
- 7-4: ScoreRule テスト → 実装
- 7-5: GameState 基本遷移（move/rotate/soft/hard/hold/tick）テスト → 実装
- 7-6: TetrominoGenerator（7-bag）テスト → 実装

## リスク・対応
- リアルタイム性: 初期はクライアント側で描画・入力処理、サーバはルール検証/状態同期。
- 仕様差異（回転/壁蹴り）: SRS簡易版を採用、拡張ポイントを設計。
- テスト負荷: ドメインにテスト集中、境界値（回転/消去/ロック遅延）を網羅。

## 次アクション（詳細設計の着手物）
- ドメイン公開APIドラフト（I/O、例外、不可変性）
- テトロミノ/ボード/ランダマイザのクラス図（テキストで良い）
- 難易度/ステージパラメータ表（重力、ロック遅延、レベルアップ条件）
- Web APIエンドポイント一覧案とステータスコード方針
