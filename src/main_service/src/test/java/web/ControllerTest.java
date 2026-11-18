package web;

import app.Application;
import app.adapter.GameAdapter;
import app.adapter.RecommendationAdapter;
import app.exceptions.WebExceptions.InternalErrorException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = Application.class)
@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private GameAdapter gameAdapter;

    @MockitoSpyBean
    private RecommendationAdapter recommendationAdapter;

    @Autowired
    private RetryRegistry retryRegistry;

    @Test
    void shouldReturnCorrectList() throws Exception {
        List<Integer> expectedGamesIds = List.of(1, 2, 3);
        List<String> expectedGamesTitles = List.of("Minecraft", "Standoff 2", "Free Fire");
        String expectedJson = "{\"gameTitles\":[\"Minecraft\",\"Standoff 2\",\"Free Fire\"]}";


        doReturn(expectedGamesIds).when(recommendationAdapter).getRecommendations();
        when(gameAdapter.getGames(any(List.class))).thenReturn(expectedGamesTitles);

        MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(response.getContentAsString()).isEqualTo(expectedJson);
    }

    @Test
    void shouldReturnSuccess() throws Exception {
        List<Integer> expectedGamesIds = new ArrayList<>();
        List<String> expectedGamesTitles = new ArrayList<>();

        doReturn(expectedGamesIds).when(recommendationAdapter).getRecommendations();
        when(gameAdapter.getGames(any(List.class))).thenReturn(expectedGamesTitles);

        MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldReturnInternal() throws Exception {
        List<Integer> expectedGamesIds = new ArrayList<>();
        List<String> expectedGamesTitles = new ArrayList<>();

        doReturn(expectedGamesIds).when(recommendationAdapter).getRecommendations();
        when(gameAdapter.getGames(any(List.class))).thenReturn(expectedGamesTitles);

        MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/exception"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    void shouldReturnTimeout() throws Exception {
        List<Integer> expectedGamesIds = new ArrayList<>();

        doReturn(expectedGamesIds).when(recommendationAdapter).getRecommendations();
        doAnswer((Answer<List<String>>) invocation -> {
            throw new SocketTimeoutException("TEST");
        }).when(gameAdapter).getGames(any(List.class));

        MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(504);
    }

    @Test
    void shouldReturnTooManyRequests() throws Exception {
        List<Integer> expectedGamesIds = new ArrayList<>();
        List<String> expectedGamesTitles = new ArrayList<>();
        int requestsCount = 11;

        doReturn(expectedGamesIds).when(recommendationAdapter).getRecommendations();
        when(gameAdapter.getGames(any(List.class))).thenReturn(expectedGamesTitles);
        int actualStatusCode = 200;
        for (int i = 0; i <= requestsCount; i++) {
            MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                    .andReturn().getResponse();
            actualStatusCode = response.getStatus();
        }

        assertThat(actualStatusCode).isEqualTo(429);
    }

    @Test

    void shouldCircuitBreakerBreakRequest() throws Exception {
        int expectedStatusCode = 500;
        ReflectionTestUtils.setField(recommendationAdapter, "fallbackEnabled", false);
        List<String> expectedGamesTitles = new ArrayList<>();
        doAnswer((Answer<List<Integer>>) invocation -> {
            throw new InternalErrorException("TEST");
        }).when(recommendationAdapter).getRecommendations();
        when(gameAdapter.getGames(any(List.class))).thenReturn(expectedGamesTitles);

        MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(expectedStatusCode);
        verify(recommendationAdapter, times(3)).getRecommendations();
        verify(gameAdapter, never()).getGames(any());
        ReflectionTestUtils.setField(recommendationAdapter, "fallbackEnabled", true);
    }

    @Test
    void shouldRetryOnInternalError() throws Exception {
        ReflectionTestUtils.setField(recommendationAdapter, "fallbackEnabled", false);
        List<String> expectedGamesTitles = List.of();

        doThrow(new InternalErrorException("TEST"))
                .doThrow(new InternalErrorException("TEST"))
                .doReturn(List.of(1, 2, 3))
                .when(recommendationAdapter).getRecommendations();

        when(gameAdapter.getGames(any(List.class))).thenReturn(expectedGamesTitles);

        MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andExpect(status().isOk())
                .andReturn().getResponse();


        verify(recommendationAdapter, times(3)).getRecommendations();
        verify(gameAdapter, times(1)).getGames(any(List.class));
        Retry retry = retryRegistry.retry("retry-recommendations");
        assertThat(retry.getMetrics().getNumberOfSuccessfulCallsWithRetryAttempt()).isEqualTo(1);
        ReflectionTestUtils.setField(recommendationAdapter, "fallbackEnabled", true);
    }

    @Test
    void shouldFallBackTest() throws Exception {
        int expectedStatusCode = 200;
        List<String> expectedGamesTitles = new ArrayList<>();
        doAnswer((Answer<List<Integer>>) invocation -> {
            throw new InternalErrorException("TEST");
        }).when(recommendationAdapter).getRecommendations();
        when(gameAdapter.getGames(any(List.class))).thenReturn(expectedGamesTitles);

        MockHttpServletResponse response = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(expectedStatusCode);
        verify(recommendationAdapter, times(3)).getRecommendations();
        verify(gameAdapter, times(1)).getGames(any());
    }

    @Test
    void shouldCaching() throws Exception {
        int exceptedStatusCode = 200;
        List<String> expectedGamesTitles = new ArrayList<>(List.of("Minecraft", "Standoff 2"));
        List<Integer> expectedGamesIds = new ArrayList<>(List.of(0, 1));
        when(recommendationAdapter.getRecommendations())
                .thenReturn(expectedGamesIds);
        doReturn(expectedGamesTitles).doThrow(new InternalErrorException("TEST"))
                .when(gameAdapter)
                .getGames(expectedGamesIds);

        MockHttpServletResponse firstResponse = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andReturn().getResponse();
        MockHttpServletResponse secondResponse = mockMvc.perform(get("/api/v1/service/games-recommendation"))
                .andReturn().getResponse();

        int actualStatusCodeFirst = firstResponse.getStatus();
        int actualStatusCodeSecond = secondResponse.getStatus();
        assertThat(actualStatusCodeFirst).isEqualTo(exceptedStatusCode);
        assertThat(actualStatusCodeSecond).isEqualTo(exceptedStatusCode);
    }
}