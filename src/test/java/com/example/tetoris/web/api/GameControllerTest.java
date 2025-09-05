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
        .andExpect(jsonPath("$.state.height", is(20)));
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
        .andExpect(jsonPath("$.state.rev", is(1)));

    // 3) GET state and compare ETag
    mockMvc
        .perform(get("/api/game/{id}/state", id))
        .andExpect(status().isOk())
        .andExpect(header().string("ETag", anyOf(is("1"), is("\"1\""))))
        .andExpect(jsonPath("$.state.rev", is(1)));
  }
}
