package com.example.tetoris.domain.rules;

import com.example.tetoris.domain.value.Position;
import com.example.tetoris.domain.value.Rotation;
import com.example.tetoris.domain.value.Size;
import com.example.tetoris.domain.value.TetrominoType;
import java.util.List;

public interface RotationSystem {
  Position spawnPosition(Size boardSize, TetrominoType type);

  List<Position> kicks(TetrominoType type, Rotation from, Rotation to);
}
