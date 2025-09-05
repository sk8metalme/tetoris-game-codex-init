## テスト設計ドラフト v0.3（2025-09-04）

本ドキュメントはMVPの品質目標（文/分岐95%+）達成に向けたテスト方針・観点・ケースを示す。実装はTDDで進め、まずdomain層を高密度に検証する。

### 変更点（v0.2）
- 実行不能入力はHTTP 200で状態無変化を検証（Web層試験に反映）。
- Rotationの境界ケースを拡張（壁/床/隅/凹み）。
- カバレッジ指標と除外ポリシーを明文化。
- 受入シナリオを3本追加（TETRIS/コンボ/ゲームオーバー）。

### 方針
- 対象優先度: domain > application > web（コントローラは最小限）。
- 粒度: 純粋関数/不変モデルを中心に単体テスト。副作用はアダプタで薄く。
- 判定: 期待状態（不変/スコア/ライン種別/キュー/hold使用制約）をスナップショットで明確化。
- カバレッジ: JaCoCoで line/branch 95%しきい値をCIで必須化。

### 設備
- フレームワーク: JUnit5（必須）、AssertJ（推奨）、Mockito（最小限; 原則domainでは不使用）。
- 実行: `gradle test`（カバレッジレポート生成）、`gradle build`で閾値検証。

### テスト観点と主要ケース

#### 1) RotationSystem（SRS簡易）
- 回転可否と壁蹴り適用順: 各ピース×4方向、境界/接地/隅/凹みのケース。
- スポーン位置: 盤面10x20での初期位置が衝突しないこと。
- ケースマトリクス（例）: piece∈{I,O,T,S,Z,J,L} × rot∈{R0→R90,R90→R180,…} × ctx∈{center,leftWall,rightWall,floor,cornerNotch}。

#### 2) Board.placeAndClear
- 1/2/3/4ライン（TETRIS）判定と消去後の重力落下。
- 境界値: 最上段/最下段/全行・空行・交互配置。
- 不変: `canPlace`真のときのみ固定できること。

#### 3) ScoreRule
- LineClearTypeごとの点数、comboボーナスの遷移（0→1→2…）。
- hard/soft dropボーナスの加算ロジック。

#### 4) TetrominoGenerator
- 7-bag前提実装の検証（同種7超の連続がない）。
- `seed`固定時の再現性、`preview(n)`の先読み整合。

#### 5) GameState 遷移
- moveLeft/Right: 壁/他ブロックで無変化、解消時の`lockTicksLeft`リセット。
- rotateCW/CCW: 壁蹴り試行、不可時は無変化（例外なし）。
- softDrop: 可能時の移動と加点/不可時の`lockTicksLeft--`。
- hardDrop: 連続落下、固定、ライン消去、加点、次ピース生成、gameOver判定。
- tick: 重力蓄積→落下→接地→ロック遅延0で固定の一連。
- holdSwap: 1ターン1回制約、空/交換の両パス、交換後のスポーン位置。
- 不変: `board`と`current`が重ならない、常に有効状態のみ返す。

- 代表受入シナリオ
  - S1: Iミノ4段消し（TETRIS）で所定スコア、ライン数+4、ステージ昇格境界に接近。
  - S2: 連続消去でcombo増加、`effectiveScore`の乗算反映。
  - S3: 杭打ちでgameOver、以降の入力が200無変化 or 410（API方針に従う）。

#### 6) ステージ/難易度
- 実効値計算: gravity/lockDelay/scoreMultiplierの反映とクリップ（lockDelay≥10）。
- 昇格条件: 累計ライン数到達時の`Stage`更新（Stage3で停止）。

#### 7) エラー/契約
- `DomainException`: 無効な盤面サイズ/不正引数。
- 入力不可は例外ではなく無変化（200想定）で返す前提の確認。

#### 8) Application層ユースケース
- startGame: seed/difficulty/stage適用、初期`GameState`妥当性。
- input: `repeat`適用、`rev`整合（If-Match不一致時の409はWeb層で確認）。

#### 9) Web層（最小）
- `/api/game/start` 正常/400（enum値不正）。
- `/api/game/{id}/input` 正常/200無変化/409（If-Match不一致）。
- `/api/game/{id}/state` 正常/404。

### データとパラメータ
- 盤面: 10x20固定、境界テスト用に最小・最大近傍配置を利用。
- 乱数: `seed`を固定し再現性を確保（例: 0, 42, 123456）。
- テトロミノ列: 代表シナリオ（TETRIS達成、コンボ、gameOver）を事前に固定列で用意。

### カバレッジ運用
- JaCoCo: line/branch 95%しきい値、未達でCI失敗。
- 除外: 例外メッセージ枝やレコードの`toString`等の自動生成コードは除外対象候補。

#### しきい値と除外（案）
- lineCoverage ≥ 0.95, branchCoverage ≥ 0.95（モジュール合計）。
- 除外候補: DTO変換の単純マッピング、`record`の自動生成アクセサ、ログ-onlyブランチ。

### 命名と構成（推奨）
- テストクラス命名: `XxxTest`、パッケージは対象と同一階層に`.test`配下。
- メソッド命名: `methodName_condition_expected()`。
- パラメタライズ: JUnit5 `@MethodSource`でケースマトリクス供給。

---

## 現状テスト実装 v0.1（2025-09-04）

- `SrsRotationSystemTest`
  - 目的: kick候補が空でないこと・(0,0)を含むこと（Tミノ R0→R90）

- `BoardPlaceAndClearTest`
  - 目的: 下段に4セルを追加してSINGLE行消去となること（詰め処理含む）

- `ScoreRuleTest`
  - 目的: `lineClearScore` の基本マッピング（NONE=0,100,300,500,800）と `comboBonus=50×combo`

- `GameStateTest`
  - 目的: `moveLeft`/`moveRight`/`softDrop`/`hardDrop` の挙動（空間での1マス移動・底まで落下）

- `TetrominoGeneratorTest`
  - 目的: 7-bagの2バッグ性（先頭14が7種×2）、seed再現性（同seed一致/異seedは高確率で不一致）

### 例外テスト（追加方針）
- `TetrominoGenerator.preview(n<0)` は `IllegalArgumentException`
- `Board`/`GameState` の契約違反（今後導入予定の `DomainException`）はユニットテストで明示

### カバレッジ現状（参考）
- 実測（2025-09-04時点）: line≈76.4% / branch≈76.4%（Jacocoレポートより）
- 上げ方: GameState未実装分のTDD継続（rotate/hold/tick/score反映）、例外/境界系の網羅


### TDD実施順（推奨）
1. `RotationSystem`/`CollisionService`の最小テスト → 実装
2. `Board.placeAndClear`の境界/代表パターン → 実装
3. `ScoreRule` → `GameState.hardDrop/softDrop/tick`の核 → 実装
4. `TetrominoGenerator`とQueue/hold → 実装
5. Applicationユースケース → Webの薄いコントローラ

### トレーサビリティ（要件→テスト）
- シングルプレイ/難易度3/ステージ3 → ステージ/難易度テスト（6）
- スコア/ライン消去 → Board(2)/ScoreRule(3)/GameState(5)
- 入力全種 → GameState(5)/Application(8)/Web(9)

### 受入判定
- 全単体テスト緑、line/branch ≥95%。
- 主要シナリオ（TETRIS/コンボ/ゲームオーバー）自動化済み。
- Web 3系エンドポイントの正常・代表異常が通過。
