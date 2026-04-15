package de.europace.bowlingbrain.game;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class GameControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/games";

    @Container
    @ServiceConnection
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        gameRepository.deleteAll();
    }

    @Test
    void shouldCreateGameAndReturnCreatedStatus() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerNames": ["Alice", "Bob"] }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.players.length()").value(2))
                .andExpect(jsonPath("$.players[0].name").value("Alice"))
                .andExpect(jsonPath("$.players[1].name").value("Bob"))
                .andExpect(jsonPath("$.currentPlayerId").isNotEmpty())
                .andExpect(jsonPath("$._links.self.href").isNotEmpty())
                .andExpect(jsonPath("$._links['next-player'].href").isNotEmpty())
                .andExpect(jsonPath("$.players[0]._links.self.href").isNotEmpty());
    }

    @Test
    void shouldSetCurrentPlayerToFirstPlayer() throws Exception {
        final String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerNames": ["Alice", "Bob"] }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        final String currentPlayerId = JsonPath.read(response, "$.currentPlayerId");
        final String firstPlayerId = JsonPath.read(response, "$.players[0].id");
        assertThat(currentPlayerId).isEqualTo(firstPlayerId);
    }

    @Test
    void shouldReturnGameById() throws Exception {
        final String createResponse = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerNames": ["Charlie"] }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        final String gameId = JsonPath.read(createResponse, "$.id");

        mockMvc.perform(get(BASE_URL + "/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.players[0].name").value("Charlie"));
    }

    @Test
    void shouldReturnNotFoundForUnknownGameId() throws Exception {
        mockMvc.perform(get(BASE_URL + "/nonexistent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAddPlayerToGame() throws Exception {
        final String gameId = createGame("Alice");

        mockMvc.perform(post(BASE_URL + "/" + gameId + "/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerName": "Bob" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.players.length()").value(2))
                .andExpect(jsonPath("$.players[1].name").value("Bob"));
    }

    @Test
    void shouldAddFrameToPlayer() throws Exception {
        final String gameId = createGame("Alice");
        final String playerId = getFirstPlayerId(gameId);

        mockMvc.perform(post(BASE_URL + "/" + gameId + "/players/" + playerId + "/frames"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.players[0].frames.length()").value(1))
                .andExpect(jsonPath("$.players[0].frames[0].id").isNotEmpty());
    }

    @Test
    void shouldAddRollToFrame() throws Exception {
        final String gameId = createGame("Alice");
        final String playerId = getFirstPlayerId(gameId);
        final String frameId = addFrameAndGetId(gameId, playerId);

        mockMvc.perform(post(BASE_URL + "/" + gameId + "/players/" + playerId + "/frames/" + frameId + "/rolls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "smashedPins": 7 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.players[0].frames[0].rolls.length()").value(1))
                .andExpect(jsonPath("$.players[0].frames[0].rolls[0].smashedPins").value(7));
    }

    @Test
    void shouldCalculateScoreAfterRoll() throws Exception {
        final String gameId = createGame("Alice");
        final String playerId = getFirstPlayerId(gameId);
        final String frameId = addFrameAndGetId(gameId, playerId);

        addRoll(gameId, playerId, frameId, 3);
        final String response = addRoll(gameId, playerId, frameId, 4);

        assertThat((Integer) JsonPath.read(response, "$.players[0].frames[0].score")).isEqualTo(7);
    }

    @Test
    void shouldAdvanceToNextPlayer() throws Exception {
        final String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerNames": ["Alice", "Bob"] }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        final String gameId = JsonPath.read(response, "$.id");
        final String firstPlayerId = JsonPath.read(response, "$.players[0].id");
        final String secondPlayerId = JsonPath.read(response, "$.players[1].id");
        assertThat((String) JsonPath.read(response, "$.currentPlayerId")).isEqualTo(firstPlayerId);

        final String nextResponse = mockMvc.perform(post(BASE_URL + "/" + gameId + "/current-player/next"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat((String) JsonPath.read(nextResponse, "$.currentPlayerId")).isEqualTo(secondPlayerId);
    }

    @Test
    void shouldWrapAroundToFirstPlayerAfterLast() throws Exception {
        final String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "playerNames": ["Alice", "Bob"] }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        final String gameId = JsonPath.read(response, "$.id");
        final String firstPlayerId = JsonPath.read(response, "$.players[0].id");

        mockMvc.perform(post(BASE_URL + "/" + gameId + "/current-player/next"));

        final String wrappedResponse = mockMvc.perform(post(BASE_URL + "/" + gameId + "/current-player/next"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat((String) JsonPath.read(wrappedResponse, "$.currentPlayerId")).isEqualTo(firstPlayerId);
    }

    @Test
    void shouldReturnNotFoundWhenAddingFrameToUnknownPlayer() throws Exception {
        final String gameId = createGame("Alice");

        mockMvc.perform(post(BASE_URL + "/" + gameId + "/players/unknown-id/frames"))
                .andExpect(status().isNotFound());
    }

    private String createGame(String... playerNames) throws Exception {
        final String body = """
                { "playerNames": ["%s"] }
                """.formatted(String.join("\",\"", playerNames));
        final String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private String getFirstPlayerId(String gameId) throws Exception {
        final String response = mockMvc.perform(get(BASE_URL + "/" + gameId))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.players[0].id");
    }

    private String addFrameAndGetId(String gameId, String playerId) throws Exception {
        final String response = mockMvc.perform(post(BASE_URL + "/" + gameId + "/players/" + playerId + "/frames"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.players[0].frames[0].id");
    }

    private String addRoll(String gameId, String playerId, String frameId, int pins) throws Exception {
        return mockMvc.perform(post(BASE_URL + "/" + gameId + "/players/" + playerId + "/frames/" + frameId + "/rolls")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"smashedPins\": " + pins + " }"))
                .andReturn().getResponse().getContentAsString();
    }
}
