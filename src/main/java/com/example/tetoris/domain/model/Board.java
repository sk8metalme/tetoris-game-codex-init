package com.example.tetoris.domain.model;

import com.example.tetoris.domain.value.LineClearType;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Size;
import java.util.List;

public interface Board {
  Size size();

  boolean isInside(Position p);

  boolean isVacant(Position p);

  boolean canPlace(Piece piece);

  PlaceResult placeAndClear(Piece piece);

  List<List<Boolean>> occupancy();

  interface PlaceResult {
    Board board();

    LineClearType lineClear();
  }
}
