package com.example.tetoris.domain.model;

import com.example.tetoris.domain.value.Position;
import java.util.List;

/** TDD便宜上の最小インターフェース。後続ステップで仕様に合わせて拡張/クラス化する。 */
public interface Piece {
  List<Position> cells(); // 盤面座標（簡易; 相対/絶対は今後調整）
}
