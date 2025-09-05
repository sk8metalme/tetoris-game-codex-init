## レビュー依頼: Web入出力 詳細化 v0.4（DTO型マッピング）

### 変更点
- DTO拡張: `GameStateDto` に `cellTypes: Map<Integer,String>` を追加（現在は {0: EMPTY, 1: LOCKED}）
- `current.type` は現状 "O" 固定（MVP）。将来ドメイン実装の型情報に同期予定。

### 相談事項
1. `cellTypes` のキー/値設計：今後 {id/name/color} の構造体に拡張するか
2. ボードの型情報が未保持なため、当面 `LOCKED` 一色（ドメイン側の型保持導入タイミング）
3. UIへの反映指針（Thymeleafで `cellTypes` を使った着色）

OKであれば v0.5 でUIの簡易描画と `current.type` の動的反映に着手します。コメントお願いします。
