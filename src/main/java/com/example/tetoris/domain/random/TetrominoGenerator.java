package com.example.tetoris.domain.random;

import com.example.tetoris.domain.value.TetrominoType;
import java.util.List;

public interface TetrominoGenerator {
  TetrominoType next();

  List<TetrominoType> preview(int count);

  TetrominoGenerator reseed(long seed);
}
