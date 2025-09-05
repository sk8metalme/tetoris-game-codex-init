package com.example.tetoris.web.api.dto;

import java.util.List;
import java.util.Map;

public record GameStateDto(
    int rev,
    int width,
    int height,
    List<List<Integer>> board,
    Map<Integer, String> cellTypes,
    CurrentPiece current,
    boolean gameOver) {

  public record CurrentPiece(String type, List<Pos> cells) {}

  public record Pos(int x, int y) {}
}
