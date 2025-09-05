package com.example.tetoris.web.api;

import com.example.tetoris.application.GameSessionService;
import com.example.tetoris.domain.model.GameState;
import com.example.tetoris.domain.value.Position;
import com.example.tetoris.web.api.dto.GameStateDto;
import com.example.tetoris.web.api.dto.InputRequest;
import com.example.tetoris.web.api.dto.StartGameRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/game")
public class GameController {
  private final GameSessionService service = new GameSessionService();

  @PostMapping("/start")
  public ResponseEntity<Map<String, Object>> start(
      @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
      @RequestBody(required = false) StartGameRequest req) {
    String id =
        service.startGameIdempotentWithOptions(
            Optional.ofNullable(req).map(StartGameRequest::boardWidth),
            Optional.ofNullable(req).map(StartGameRequest::boardHeight),
            Optional.ofNullable(req).map(StartGameRequest::seed),
            Optional.ofNullable(req).map(StartGameRequest::difficulty),
            Optional.ofNullable(idemKey));
    var session = service.get(id);
    return ResponseEntity.status(HttpStatus.CREATED)
        .header(HttpHeaders.ETAG, String.valueOf(session.rev()))
        .body(
            Map.of(
                "id",
                id,
                "state",
                toDto(session.rev(), session.state()),
                "score",
                session.score(),
                "combo",
                session.combo()));
  }

  @PostMapping("/{id}/input")
  public ResponseEntity<Map<String, Object>> input(
      @PathVariable String id,
      @RequestHeader(value = "If-Match", required = false) String ifMatch,
      @RequestHeader(value = "Idempotency-Key", required = false) String idemKey,
      @RequestBody InputRequest req) {
    var current = service.get(id);
    if (ifMatch != null && !matchesRev(ifMatch, current.rev())) {
      throw new ConflictException("rev mismatch: expected If-Match=" + current.rev());
    }
    var session =
        (idemKey != null)
            ? service.applyIdempotent(
                id, idemKey, req.action(), Optional.ofNullable(req.repeat()).orElse(1))
            : service.apply(id, req.action(), Optional.ofNullable(req.repeat()).orElse(1));
    return ResponseEntity.ok()
        .header(HttpHeaders.ETAG, String.valueOf(session.rev()))
        .body(
            Map.of(
                "state",
                toDto(session.rev(), session.state()),
                "score",
                session.score(),
                "combo",
                session.combo()));
  }

  @GetMapping("/{id}/state")
  public ResponseEntity<Map<String, Object>> state(@PathVariable String id) {
    var session = service.get(id);
    return ResponseEntity.ok()
        .header(HttpHeaders.ETAG, String.valueOf(session.rev()))
        .body(
            Map.of(
                "state",
                toDto(session.rev(), session.state()),
                "score",
                session.score(),
                "combo",
                session.combo()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(GameSessionService.NotFoundException.class)
  public ResponseEntity<ApiError> notFound(GameSessionService.NotFoundException e) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(new ApiError("GAME_NOT_FOUND", e.getMessage()));
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiError> conflict(ConflictException e) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ApiError("CONFLICT", e.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> badRequest(IllegalArgumentException e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiError("INVALID_REQUEST", e.getMessage()));
  }

  private static GameStateDto toDto(int rev, GameState s) {
    // board: 固定ブロックの0/1表現
    List<List<Integer>> board = new ArrayList<>();
    for (var row : s.board().occupancy()) {
      List<Integer> r = new ArrayList<>(row.size());
      for (Boolean b : row) r.add(Boolean.TRUE.equals(b) ? 1 : 0);
      board.add(r);
    }
    // current
    List<GameStateDto.Pos> cells = new ArrayList<>();
    for (Position p : s.current().cells()) {
      cells.add(new GameStateDto.Pos(p.x(), p.y()));
    }
    var cp = new GameStateDto.CurrentPiece("O", cells); // MVP: O固定
    int height = board.size();
    int width = height == 0 ? 0 : board.get(0).size();
    Map<Integer, String> types = Map.of(0, "EMPTY", 1, "LOCKED");
    return new GameStateDto(rev, width, height, board, types, cp, false);
  }

  private static boolean matchesRev(String ifMatch, int rev) {
    String v = ifMatch.trim();
    if (v.startsWith("\"") && v.endsWith("\"")) {
      v = v.substring(1, v.length() - 1);
    }
    return v.equals(String.valueOf(rev));
  }

  public record ApiError(String code, String message) {}

  public static final class ConflictException extends RuntimeException {
    public ConflictException(String message) {
      super(message);
    }
  }
}
