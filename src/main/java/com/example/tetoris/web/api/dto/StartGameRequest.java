package com.example.tetoris.web.api.dto;

public record StartGameRequest(
    Integer boardWidth, Integer boardHeight, String difficulty, Integer stage, Long seed) {}
