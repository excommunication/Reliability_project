package app.web;

import app.adapter.GameAdapter;
import app.adapter.RecommendationAdapter;
import app.dto.GamesResponse;
import app.exceptions.WebExceptions.InternalErrorException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/service")
public class MainController {

    private final GameAdapter gameAdapter;
    private final RecommendationAdapter recommendationAdapter;

    @GetMapping("/games-recommendation")
    @RateLimiter(name = "limiter")
    public GamesResponse getGamesRecommendation() {
        return new GamesResponse(
                gameAdapter.getGames(
                        recommendationAdapter.getRecommendations()
                )
        );
    }

    @GetMapping("/exception")
    public void getException() {
        throw new InternalErrorException("Internal Error");
    }
}
