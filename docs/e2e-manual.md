## E2E 手動確認チェックリスト（v0.1 — 2025-09-05）

対象: MVP版（Web/API v0.5〜0.7、ドメイン基本操作）

### 前提
- Java 21 / Gradle がローカルにインストール済み
- ポート: 8080（未使用）
- ブラウザ: 最新版 Chrome / Edge（DevTools利用）

### 起動
1. ターミナルで `gradle bootRun`
2. `Started ...` ログを確認
3. ブラウザで http://localhost:8080/ を開く（タイトル Tetris MVP を表示）

### 画面初期表示（UI）
- [ ] Start ボタンが表示される
- [ ] 空の盤面（10×20）が描画されている（グリッド）
- [ ] ステータスに Ready と表示

### ゲーム開始（Start）
1. Start をクリック
2. DevTools Network で `POST /api/game/start` を選択
   - [ ] Status 201
   - [ ] Response Headers に `ETag: 0`
   - [ ] Response JSON に `id` と `state.rev=0` が含まれる
3. 盤面に current（黄色枠の2×2 O）を表示
   - [ ] DOM `.cell.curr` が4マスに付与

### 入力（キーボード）
- 右移動
  1. → を1回押下
  2. Network `POST /api/game/{id}/input`
     - [ ] Request Headers に `If-Match: 0`
     - [ ] Response 200 / `ETag: 1`
  3. current が右に1マス移動
- ソフトドロップ
  1. ↓ を1回押下
  2. [ ] Response 200 / `ETag` が +1 される
  3. current が下に1マス移動
- ハードドロップ
  1. Space を1回押下
  2. [ ] Response 200 / `ETag` が +1 される
  3. current が最下段まで落下

### 競合検知（If-Match）
1. タブAで `GET /api/game/{id}/state` を取得（`ETag: N`）
2. タブBで操作して `ETag: N+1` に更新
3. タブAで古い `If-Match: N` で `POST /input`
   - [ ] 409 CONFLICT (`code=CONFLICT`) を受け取る
4. タブAで再取得 `GET /state`
   - [ ] `ETag: N+1` を取得し、以後の操作が成功する

### 冪等性（Idempotency-Key）
1. DevTools で `POST /start` を2回送信（同一 `Idempotency-Key`）
   - [ ] 同じ `id` が返る（新規作成されない）
2. `POST /input` を2回送信（同一 `Idempotency-Key`）
   - [ ] 同じ `rev/state` が返る（重複適用されない）

### 色マッピング（v0.7）
- [ ] ロックブロック: `.cell.type-locked` が水色で表示
- [ ] current: `.cell.curr` が枠線で強調
- [ ] 将来の型拡張に備え `.cell.type-i/o/t/s/z/j/l` クラス定義が存在

### API 単体確認（curl 例）
```bash
# 起動状態で実行
curl -s -i -H 'Content-Type: application/json' -X POST \
  http://localhost:8080/api/game/start -d '{}' | sed -n '1,20p'

ID=...; ET=0
curl -s -i http://localhost:8080/api/game/$ID/state | sed -n '1,5p'
curl -s -i -H 'Content-Type: application/json' -H "If-Match: $ET" -X POST \
  http://localhost:8080/api/game/$ID/input -d '{"action":"MOVE_RIGHT","repeat":1}' | sed -n '1,5p'
```

### 例外系
- [ ] 存在しないID → 404 `GAME_NOT_FOUND`
- [ ] `If-Match` 不一致 → 409 `CONFLICT`
- [ ] 不正リクエスト（body欠落/型不正）→ 400 `INVALID_REQUEST`

### トラブルシュート
- 起動しない: ポート 8080 占有を確認、Java 21 の PATH 設定
- 画面が更新されない: DevTools で JS エラー（Console）と Network レスポンスを確認
- 409が頻発: 画面リロードまたは最新 `ETag` で再送

### 記録テンプレート
- 実施者/日時: 
- ブラウザ/OS: 
- 成否: 初期表示 [ ] / Start [ ] / 操作（→/↓/Space） [ ] / 競合 [ ] / 冪等 [ ] / 色 [ ]
- 所見/改善要望:

