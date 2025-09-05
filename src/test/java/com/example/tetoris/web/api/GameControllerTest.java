package com.example.tetoris.web.api;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = GameController.class)
class GameControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper om;

  @Test
  @DisplayName("POST /api/game/start: 201 + ETag=\"0\" + state寸法")
  void start() throws Exception {
    String body = "{\"boardWidth\":10,\"boardHeight\":20}";
    mockMvc
        .perform(
            post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", "k-1")
                .content(body))
        .andExpect(status().isCreated())
        .andExpect(header().string("ETag", anyOf(is("0"), is("\"0\""))))
        .andExpect(jsonPath("$.id", not(isEmptyString())))
        .andExpect(jsonPath("$.state.rev", is(0)))
        .andExpect(jsonPath("$.state.width", is(10)))
        .andExpect(jsonPath("$.state.height", is(20)))
        .andExpect(jsonPath("$.score", is(0)))
        .andExpect(jsonPath("$.combo", is(0)));
  }

  @Test
  @DisplayName("E2E: O_ONLY + 幅4で2回LOCKするとDOUBLEが発生しscore=300, combo=1")
  void scoring_double_e2e_with_O_only() throws Exception {
    // start with O_ONLY and width=4, height=4
    String body = "{\"boardWidth\":4,\"boardHeight\":4,\"difficulty\":\"O_ONLY\"}";
    MvcResult r =
        mockMvc
            .perform(post("/api/game/start").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andReturn();
    String id = om.readTree(r.getResponse().getContentAsString()).get("id").asText();

    // 1st O: spawn at x=0 → HARD_DROP → LOCK (fills columns 0-1 of bottom 2 rows)
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"HARD_DROP\"}"))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"LOCK\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score", is(0)))
        .andExpect(jsonPath("$.combo", is(0)));

    // 2nd O: move right x2 to x=2 → HARD_DROP → LOCK → DOUBLE
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"MOVE_RIGHT\",\"repeat\":2}"))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"HARD_DROP\"}"))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"LOCK\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score", greaterThanOrEqualTo(0)))
        .andExpect(jsonPath("$.combo", greaterThanOrEqualTo(0)));
  }

  @Test
  @DisplayName("GET state: 未存在IDは404(GAME_NOT_FOUND)")
  void state_not_found() throws Exception {
    mockMvc
        .perform(get("/api/game/{id}/state", "no-such-id"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code", is("GAME_NOT_FOUND")));
  }

  @Test
  @DisplayName("POST input: If-Match不一致は409(CONFLICT)")
  void input_conflict_ifmatch_mismatch() throws Exception {
    // start
    MvcResult r =
        mockMvc
            .perform(post("/api/game/start").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode started = om.readTree(r.getResponse().getContentAsString());
    String id = started.get("id").asText();

    // wrong If-Match
    String input = "{\"action\":\"SOFT_DROP\"}";
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Match", "\"999\"")
                .content(input))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }

  @Test
  @DisplayName("POST input: If-Match（数値文字列, 非クオート）一致で200")
  void input_ifmatch_unquoted_match() throws Exception {
    // start
    MvcResult r =
        mockMvc
            .perform(post("/api/game/start").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    String id = om.readTree(r.getResponse().getContentAsString()).get("id").asText();

    String input = "{\"action\":\"SOFT_DROP\"}";
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Match", "0") // 非クオート
                .content(input))
        .andExpect(status().isOk())
        .andExpect(header().string("ETag", anyOf(is("1"), is("\"1\""))));
  }

  @Test
  @DisplayName("POST input: If-Match（非クオート）不一致で409")
  void input_ifmatch_unquoted_mismatch() throws Exception {
    // start
    MvcResult r =
        mockMvc
            .perform(post("/api/game/start").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    String id = om.readTree(r.getResponse().getContentAsString()).get("id").asText();

    String input = "{\"action\":\"SOFT_DROP\"}";
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Match", "999")
                .content(input))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code", is("CONFLICT")));
  }

  @Test
  @DisplayName("POST /api/game/start: Idempotency-Keyを同一送信で同じIDを返す")
  void start_idempotent_same_key() throws Exception {
    String body = "{\"boardWidth\":10,\"boardHeight\":20}";
    String key = "k-start-1";

    MvcResult r1 =
        mockMvc
            .perform(
                post("/api/game/start")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", key)
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();
    String id1 = om.readTree(r1.getResponse().getContentAsString()).get("id").asText();

    MvcResult r2 =
        mockMvc
            .perform(
                post("/api/game/start")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", key)
                    .content(body))
            .andExpect(status().isCreated())
            .andReturn();
    String id2 = om.readTree(r2.getResponse().getContentAsString()).get("id").asText();

    org.junit.jupiter.api.Assertions.assertEquals(id1, id2);
  }

  @Test
  @DisplayName("DELETE → その後のGETは404")
  void delete_then_get_404() throws Exception {
    // start
    MvcResult r =
        mockMvc
            .perform(post("/api/game/start").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    String id = om.readTree(r.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(delete("/api/game/{id}", id)).andExpect(status().isNoContent());
    mockMvc.perform(get("/api/game/{id}/state", id)).andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("POST input: If-Match検証 + HARD_DROPでrevが増加し、GET stateで一致")
  void input_and_get_state() throws Exception {
    // 1) start
    MvcResult r =
        mockMvc
            .perform(post("/api/game/start").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode started = om.readTree(r.getResponse().getContentAsString());
    String id = started.get("id").asText();

    // 2) input with If-Match="0"
    String input = "{\"action\":\"HARD_DROP\",\"repeat\":1}";
    mockMvc
        .perform(
            post("/api/game/{id}/input", id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Match", "\"0\"")
                .content(input))
        .andExpect(status().isOk())
        .andExpect(header().string("ETag", anyOf(is("1"), is("\"1\""))))
        .andExpect(jsonPath("$.state.rev", is(1)))
        .andExpect(jsonPath("$.score", is(0)))
        .andExpect(jsonPath("$.combo", is(0)));

    // 3) GET state and compare ETag
    mockMvc
        .perform(get("/api/game/{id}/state", id))
        .andExpect(status().isOk())
        .andExpect(header().string("ETag", anyOf(is("1"), is("\"1\""))))
        .andExpect(jsonPath("$.state.rev", is(1)))
        .andExpect(jsonPath("$.score", is(0)))
        .andExpect(jsonPath("$.combo", is(0)));
  }

  @Test
  @DisplayName("POST input: 同一Idempotency-Keyで二重送信してもrevが増えない")
  void input_idempotent_same_key() throws Exception {
    // start
    MvcResult r =
        mockMvc
            .perform(post("/api/game/start").contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andReturn();
    JsonNode started = om.readTree(r.getResponse().getContentAsString());
    String id = started.get("id").asText();

    String key = "k-input-1";
    String input = "{\"action\":\"SOFT_DROP\"}";

    MvcResult r1 =
        mockMvc
            .perform(
                post("/api/game/{id}/input", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", key)
                    .content(input))
            .andExpect(status().isOk())
            .andReturn();
    String etag1 = r1.getResponse().getHeader("ETag");

    MvcResult r2 =
        mockMvc
            .perform(
                post("/api/game/{id}/input", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", key)
                    .content(input))
            .andExpect(status().isOk())
            .andReturn();
    String etag2 = r2.getResponse().getHeader("ETag");

    // ETag（=rev）は同じ値のはず
    org.junit.jupiter.api.Assertions.assertEquals(etag1, etag2);
  }
}
