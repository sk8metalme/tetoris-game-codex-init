## レビュー依頼: Web/API スケルトン v0.1

### 追加内容
- Controller: `GameController`（/api/game/start, /{id}/input, /{id}/state, /{id}）
- DTO: `GameStateDto`, `StartGameRequest`, `InputRequest`
- Service: `GameSessionService`（メモリ内セッション、Oブロック固定スポーン、基本操作適用）

### 方針
- DTOはMVP簡易版（boardは0/1、currentはtype="O"+cells）。将来、色/型マッピングへ拡張予定。
- エラーポリシー: `NotFound`→404、未対応アクション→無変化200（MVP方針に一致）

### 確認事項
1. エンドポイントIF（JSON形状/ステータス）の初期版として妥当か
2. `GameSessionService`の責務とスコープ（in-memory, revインクリメント）
3. DTOの最小構成（board 0/1, current cells）での当面の運用可否

改善提案があればコメントお願いします。問題なければ v0.2 で入出力の詳細を固めます。
