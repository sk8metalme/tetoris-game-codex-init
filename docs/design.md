# 詳細設計: ドメイン公開APIドラフト v0.1 （2025-09-04）

本ドキュメントは、オニオンアーキテクチャの最内層（domain）における公開APIの骨子を示す。実装はTDDで行い、ここで規定するI/O、例外、不可変性を守る。

## 設計原則
- 不可変志向: 値オブジェクトと`GameState`は原則として新インスタンスを返す。
- 副作用の分離: 乱数シード・時刻等の外部要因は引数で受ける。
- シンプルなSRS（回転/壁蹴り）を採用し、拡張ポイントを用意。
- UI/入出力・永続化・フレームレートはdomainに持ち込まない。

## パッケージ構成（domain）
- `...domain.value` — 値オブジェクト（`Position`, `Size`, `Rotation`, ほか）
- `...domain.model` — 集約（`Board`, `Piece`, `GameState`）
- `...domain.random` — 乱数・順序（`TetrominoGenerator`）
- `...domain.rules` — ルール（`Rules`, `RotationSystem`, `ScoreRule`）
- `...domain.service` — 純粋ドメインサービス（`CollisionService`, `LineClearService`）

## 主要型と公開API（ドラフト）

### 値オブジェクト（`...domain.value`）
```java
public record Position(int x, int y) {}

public record Size(int width, int height) {
  public static Size of(int width, int height);
}

public enum Rotation { R0, R90, R180, R270 }

public enum TetrominoType { I, O, T, S, Z, J, L }

public enum LineClearType { NONE, SINGLE, DOUBLE, TRIPLE, TETRIS }

public enum Difficulty { EASY, NORMAL, HARD }

public record Stage(int index) {
  public static Stage of(int index);
}
```

### ルール（`...domain.rules`）
```java
public interface RotationSystem {
  /** スポーン位置（盤面サイズ依存）。*/
  Position spawnPosition(Size boardSize, TetrominoType type);

  /** 壁蹴り候補（from→to）。先頭から順に試行。*/
  List<Position> kicks(TetrominoType type, Rotation from, Rotation to);
}

public record Rules(
    Difficulty difficulty,
    Stage stage,
    double gravityPerTick,     // 1tickあたり落下セル量（例: 1.0で1セル）
    int lockDelayTicks,        // ロック遅延tick
    int softDropBonusPerCell,  // ソフトドロップ加点
    int hardDropBonusPerCell   // ハードドロップ加点
) {
  public static Rules of(Difficulty difficulty, Stage stage,
      double gravityPerTick, int lockDelayTicks,
      int softDropBonusPerCell, int hardDropBonusPerCell);
}

public interface ScoreRule {
  /** ライン消去時の基本スコア。*/
  int lineClearScore(LineClearType type);
  /** 連続消去等のボーナス（将来拡張用）。*/
  default int comboBonus(int combo){ return 0; }
}
```

### ランダマイザ（`...domain.random`）
```java
public interface TetrominoGenerator {
  TetrominoType next();
  List<TetrominoType> preview(int count);
  TetrominoGenerator reseed(long seed); // 新インスタンスを返す
}
```

### モデル（`...domain.model`）
```java
public final class Piece {
  public TetrominoType type();
  public Rotation rotation();
  public Position position();

  public Piece withRotation(Rotation rotation);
  public Piece withPosition(Position position);
  /** ピースを構成する相対セル座標（原点はposition）。*/
  public List<Position> cells();
}

public interface Board {
  Size size();
  /** 盤面内か？ */
  boolean isInside(Position p);
  /** セルが空か？ */
  boolean isVacant(Position p);
  /** 指定ピースを衝突なく配置可能か？ */
  boolean canPlace(Piece piece);
  /** ピースの固定とライン消去を適用した新盤面を返す。*/
  PlaceResult placeAndClear(Piece piece);
  /** 盤面のスナップショット（描画用）。*/
  List<List<Boolean>> occupancy();

  interface PlaceResult {
    Board board();
    LineClearType lineClear();
  }
}

public final class Score {
  public int points();
  public int lines();
  public int combo();
  public Score add(int deltaPoints, int deltaLines, int nextCombo);
}

public final class GameState {
  public Board board();
  public Piece current();
  public Optional<TetrominoType> hold();
  public List<TetrominoType> queue(); // 先読み（例: 最大5）
  public Score score();
  public Rules rules();
  public boolean gameOver();

  // 状態遷移（すべて新インスタンスを返す）
  public GameState moveLeft();
  public GameState moveRight();
  public GameState rotateCW();
  public GameState rotateCCW();
  public GameState softDrop(); // 1tick相当の落下
  public GameState hardDrop(); // 一気に接地・ロック
  public GameState holdSwap();
  public GameState tick();     // 重力・ロック遅延処理

  // ファクトリ
  public static GameState start(Size boardSize, Rules rules, TetrominoGenerator rng,
      RotationSystem srs, ScoreRule scoreRule);
}
```

### 例外（共通）
```java
public class DomainException extends RuntimeException {
  public DomainException(String message) { super(message); }
}

public final class InvalidMoveException extends DomainException {
  public InvalidMoveException(String message) { super(message); }
}
```

## 不変条件・境界
- `Board`は座標が`[0,width) x [0,height)`のときのみ`isInside`が真。
- `GameState.hardDrop()`は少なくとも1行の移動を保証し、固定後に`LineClearType`を評価。
- ロック遅延は`tick()`内で管理し、入力による再浮上は許可しない（MVP）。
- `TetrominoGenerator`は7-bag前提の実装を推奨（仕様上は強制しない）。

## テスト観点（抜粋）
- 回転（SRS簡易）の壁蹴り候補が順序通りに適用されること。
- `placeAndClear`のライン種別判定と`ScoreRule`適用。
- `hardDrop`時の加点とコンボの推移。
- 重力・ロック遅延の境界値（0/1/N tick）。

## 今後の詳細化ポイント（別ステップで実施）
- ドメイン責務/相互作用（サービス分割と副作用境界）
- 難易度/ステージ別パラメータテーブル
- Web API入出力スキーマ案

---

## 実装状況 v0.1（2025-09-04）

- ランダマイザ: `SevenBagGenerator` 実装済み。`Generators.sevenBag(seed)` で提供。`preview(n)` は非消費・不足時のみ補充。`reseed(seed)` は新インスタンス。
- 盤面: `GridBoard` 実装（`isVacant`/`canPlace`/`placeAndClear`）。複数行消去と下詰めに対応。返値で `LineClearType` を返却。
- スコア: `BasicScoreRule` 実装（NONE=0, 100/300/500/800、combo=50×combo）。
- GameState: `moveLeft`/`moveRight`/`softDrop`/`hardDrop` を実装（不可時は無変化）。`rotateCW/CCW`、`holdSwap`、`tick`、`score`反映、ロック遅延は未実装。
- 回転系: `SrsRotationSystem` は最小kick表を実装（今後、正確なSRS値への拡張余地あり）。
- 難易度/ステージ: パラメータ定義は策定済み（docs）。実配線は未着手。
- Web/API: エンドポイント案は策定済み（docs）。実装は未着手。

### 既知の未実装/課題
- `GameState.tick`（重力・ロック遅延管理）、`holdSwap`、回転（正確なSRS適用）
- スコア更新（line clear/soft/hard/コンボ/ステージ倍率）と`Score`集約
- Web層（コントローラ/DTO変換）とアプリケーション層のユースケース実装
- 難易度/ステージの実行時反映（effective値の計算と更新）


## ドメイン責務/相互作用（v0.1）

本節では、各型の責務境界と代表的なフロー（相互作用）を定義する。

### 責務境界
- **GameState（オーケストレーター）**: 入力（move/rotate/drop/tick/hold）を受け、`CollisionService`/`LineClearService`/`RotationSystem`/`ScoreRule`を用いて純粋遷移を実行。ロック遅延・重力蓄積・ホールド使用状態・キュー消費を管理。`TetrominoGenerator`の内部状態はGameStateが保持（非公開）。
- **Board（不変盤面）**: セル占有状態を表現。固定・消去の計算は`placeAndClear`で完結させ、結果を`PlaceResult`として返す。外部からは`canPlace`/`isVacant`等の照会のみ。
- **Piece（不変ピース）**: 種別・回転・位置の表現とセル展開（`cells()`）。ロジックは持たない（回転適用は`RotationSystem`の責務）。
- **RotationSystem（SRS簡易）**: 回転に伴う壁蹴りオフセット列の提供とスポーン位置の決定。
- **CollisionService**: `Board`と`Piece`の衝突可否の判定（`canPlace`を補助）。最終判断は`Board#canPlace`。
- **LineClearService**: 固定後のライン消去と`LineClearType`の算出（実体は`Board#placeAndClear`内から利用）。
- **ScoreRule**: ライン種別・ドロップボーナス・コンボ加点のポリシー提供。
- **TetrominoGenerator**: 次ピースの供給。7-bag推奨。`GameState`内で新状態へ遷移。

### 状態と不変条件（GameState 内部）
- `gravityAcc`: 重力蓄積の小数保持（例: 0.0..1.0未満）。
- `lockTicksLeft`: 接地後のロック遅延残りtick。移動/回転で接地を解消した場合はリセット。
- `holdUsedThisTurn`: 固定までの間にホールドを1回のみ許可。
- `queue`: 先読みリスト（例: 5）。不足時に`TetrominoGenerator`から補充。
- 不変: `board`と`current`の重なり無し、`current.cells()`は常に`board`内に配置可能。

### 代表フロー（疑似シーケンス）
- moveLeft/moveRight
  1) `next = current.withPosition(±1,0)`
  2) `board.canPlace(next)`なら`current = next`、でなければ無変化
  3) 接地解消なら`lockTicksLeft = rules.lockDelayTicks`

- rotateCW/rotateCCW
  1) `targetRot = ...`、`kicks = srs.kicks(type, from, to)`
  2) 各`k`について`next = current.withRotation(targetRot).withPosition(pos+ k)`
  3) `board.canPlace(next)`が真の最初を採用。無ければ無変化（例外非推奨）
  4) 接地解消なら`lockTicksLeft`をリセット

- softDrop
  1) `tryDown = current.y + 1`
  2) 可能なら移動し`score += rules.softDropBonusPerCell`、不可能なら`lockTicksLeft--`（下限0）

- hardDrop
  1) `steps = 最大まで落下`
  2) `score += steps * rules.hardDropBonusPerCell`
  3) 固定→`Board.placeAndClear`→`LineClearType`反映→`ScoreRule`で加点
  4) `holdUsedThisTurn = false`、新ピースをスポーン。不能なら`gameOver = true`

- tick（重力・ロック）
  1) `gravityAcc += rules.gravityPerTick`
  2) `while gravityAcc >= 1: try move down` 成功なら`gravityAcc -= 1`、失敗なら接地とみなし`lockTicksLeft--`
  3) `lockTicksLeft == 0`で固定→ライン処理→スコア→次ピーススポーン

- holdSwap
  1) `holdUsedThisTurn`が真なら無変化
  2) 空なら`hold = current.type`、`current = queue.pop()`をスポーン位置へ
  3) 空でなければ`current.type`と`hold`を交換しスポーン位置へ
  4) `holdUsedThisTurn = true`

### エラーポリシー
- 入力由来の不成立（移動不可・回転不可）は例外としない（無変化返却）。
- API契約違反（null, 無効な盤面サイズなど）は`DomainException`。

### テスト方針（責務別）
- GameState: フロー別スナップショット（状態遷移の最小構成）
- Board: `placeAndClear`の網羅（境界/全行/空行）
- RotationSystem: 各ピースのkick表順序適用
- ScoreRule: ライン種別・コンボ・ドロップ加点の計算

---

## 難易度・ステージパラメータ定義 v0.1

本節では、`Rules`に与える初期パラメータと、`Stage`に応じた補正値を定義する。数値は初期チューニング値であり、テストとプレイフィールに応じて微調整する。

### 難易度プリセット（Rules 基本値）
単位は「セル / tick」。`tick()`の呼び出し周期はアプリケーション層で決定する。

```
EASY:
  gravityPerTick:        0.50
  lockDelayTicks:        60
  softDropBonusPerCell:   1
  hardDropBonusPerCell:   2

NORMAL:
  gravityPerTick:        1.00
  lockDelayTicks:        40
  softDropBonusPerCell:   1
  hardDropBonusPerCell:   2

HARD:
  gravityPerTick:        1.50
  lockDelayTicks:        25
  softDropBonusPerCell:   1
  hardDropBonusPerCell:   3
```

### ステージ補正（3ステージ固定）
各ステージは難易度基本値に対して乗算/加算で補正を行う。

```
Stage 1:
  gravityMultiplier: 1.00
  lockDelayDelta:    +0     # 変更なし
  scoreMultiplier:   1.00
  linesToNext:       10

Stage 2:
  gravityMultiplier: 1.25
  lockDelayDelta:    -5
  scoreMultiplier:   1.10
  linesToNext:       10

Stage 3:
  gravityMultiplier: 1.50
  lockDelayDelta:    -10
  scoreMultiplier:   1.20
  linesToNext:       N/A   # 最終ステージ
```

### 実効値の計算規則（擬似）
```
effectiveGravityPerTick = base.gravityPerTick * stage.gravityMultiplier
effectiveLockDelayTicks = max(10, base.lockDelayTicks + stage.lockDelayDelta)
effectiveScore(points)  = floor(points * stage.scoreMultiplier)
```

`linesToNext`は累計消去ライン数に基づくステージ昇格条件で、昇格時に`Stage`を更新する（実装はapplication層）。

### デフォルトの盤面とキュー長（参考値）
- Board size: 10x20（固定）
- Queue preview: 5
- Hold: 1回/固定までに1回のみ

### テスト観点（パラメータ）
- ステージ昇格境界（9→10、19→20行）での`effective*`再計算。
- `lockDelayTicks`の下限クリップ（10tick）動作。
- 難易度間の相対差（HARDの方がEASYより速く、遅延が短い）。

---

## Web APIエンドポイント案 v0.1

### 基本方針
- REST + JSON。サーバは状態遷移の検証と保持を担当、描画はクライアント（Thymeleaf + JS）。
- 初期版はポーリングで十分。SSE/WebSocketは将来拡張。
- 入力の冪等化に`Idempotency-Key`（任意）対応。

### 共通
- Base Path: `/api/game`
- 成功: 200/201/204、エラー: 400/404/409/410/422
- `Content-Type: application/json`
- エラー形式:
```json
{ "error": { "code": "INVALID_ACTION", "message": "...", "details": {} } }
```
エラーコード例: `INVALID_REQUEST`, `GAME_NOT_FOUND`, `INVALID_ACTION`, `GAME_OVER`, `CONFLICT`, `RATE_LIMITED`。

### エンドポイント
- POST `/api/game/start`
  - Body:
```json
{ "difficulty": "EASY|NORMAL|HARD", "stage": 1, "seed": 123456, "boardWidth": 10, "boardHeight": 20 }
```
  - Resp: 201 Created
```json
{ "id": "g_abc123", "state": { /* GameStateDTO */ } }
```

- POST `/api/game/{id}/input`
  - Header: `Idempotency-Key: <uuid>`（任意）
  - Body:
```json
{ "action": "MOVE_LEFT|MOVE_RIGHT|ROTATE_CW|ROTATE_CCW|SOFT_DROP|HARD_DROP|HOLD|TICK", "repeat": 1 }
```
  - Resp: 200 OK → `{ "state": { /* GameStateDTO */ } }`
  - 409: `If-Match`と`rev`不一致などの競合

- GET `/api/game/{id}/state`
  - Query: `rev`（省略可）
  - Resp: 200 OK → `{ "state": { /* GameStateDTO */ } }`

- DELETE `/api/game/{id}`
  - Resp: 204 No Content（存在しなくても冪等で204）

### DTO（転送用）: `GameStateDTO`
domain→DTO変換はアダプタ層で実施。
```json
{
  "rev": 3,
  "board": [[0,0,0,0,0,0,0,0,0,0], ...],
  "current": { "type": "T", "rotation": "R90", "position": {"x":4,"y":0}, "cells": [{"x":3,"y":0},{"x":4,"y":0},{"x":5,"y":0},{"x":4,"y":1}] },
  "hold": "I",
  "queue": ["S","Z","J","L","O"],
  "score": { "points": 1200, "lines": 4, "combo": 1 },
  "rules": { "difficulty": "NORMAL", "stage": 2, "gravityPerTick": 1.25, "lockDelayTicks": 35, "softDropBonusPerCell": 1, "hardDropBonusPerCell": 2 },
  "gameOver": false
}
```
- `board`のセル: 0=空, 1..7=I,O,T,S,Z,J,L（マッピングはプレゼン層定数）
- `rev`: 楽観ロック用。`If-Match: <rev>`で競合検知（任意）。

### ユースケース対応
- start → `application.startGame()`
- input(action,repeat) → `application.move/rotate/.../tick()`の逐次適用
- state → 現在`GameState`のスナップショット取得
- delete → セッション破棄

### ステータスコード詳細
- 400 INVALID_REQUEST: パラメータ不正（JSON構造/enum値/範囲エラー）
- 404 GAME_NOT_FOUND: id不正/期限切れ
- 409 CONFLICT: `rev`不一致 等
- 410 GAME_OVER: 終了後の入力
- 422 INVALID_ACTION: 契約違反のアクション（未知のactionなど）。
- 200 OK（無変化）: 実行不能な入力（衝突/境界/回転不可）はMVPでは成功扱いで状態不変を返す。

### セキュリティ/運用（初期）
- 認証なし（ローカル開発）。将来はSession/Cookieまたはトークン。
- Rate Limit（目安）: 10 req/sec/ゲーム

---

## 詳細設計レビュー/改善 v0.1（2025-09-04）

### 整合性チェック（結果）
- API ⇔ アプリ層ユースケース: start/input/state/delete が `startGame/move/rotate/softDrop/hardDrop/hold/tick/getState/delete` に1対1対応（OK）。
- DTO ⇔ domain: `GameState`は表示用に`GameStateDTO`へ変換。boardは0..7のint表現で不変（OK）。
- ルール ⇔ 難易度/ステージ: 実効値計算規則で矛盾なし（OK）。

### 決定事項（改善）
- 実行不能入力はHTTP 200で無変化を返す（ドメイン方針と一致）。
- 422はスキーマ/列挙値不正のみで使用。ドメイン上の「不可」は422にしない。
- `Idempotency-Key`はゲームIDと組で重複入力を抑止（同一レスポンスを返す）。
- `rev`はレスポンス単調増加。`If-Match`不一致時は409。
- ステージ昇格: 累計ライン数が`linesToNext`到達で+1。Stage3で打ち止め。

### 未決事項（次工程で検討）
- スコア詳細式（Tetris/Back-to-Back/Comboの具体値）：テスト設計でFIX。
- SRS kick表の具体値：実装直前にFIX（参考資料に基づく）。
- セッション保持方式（メモリ/簡易永続化）：スケルトン実装時に決定。

### チェックリスト
- [x] API-ドメイン対応の確認
- [x] エラーポリシー確定（200無変化/422/409）
- [x] DTO方針（int board, rev）
- [x] ステージ昇格・最終段の扱い
- [ ] スコア式の係数FIX（保留）
