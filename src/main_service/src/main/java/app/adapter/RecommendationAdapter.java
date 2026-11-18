package app.adapter;

import app.dto.RecommendationsResponse;
import app.exceptions.WebExceptions.BadRequestException;
import app.exceptions.WebExceptions.InternalErrorException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RecommendationAdapter {

    @Value("${app.fallback.enabled:true}")
    private boolean fallbackEnabled;

    @Value("${rest.client.base-uri-recommendations}")
    private String baseUri;

    private final RetryRegistry retryRegistry;

    private RestClient client;

    @jakarta.annotation.PostConstruct
    public void init() {
        ClientFactory clientFactory = new ClientFactory();
        this.client = clientFactory.getRestClient(baseUri);
    }

    @PostConstruct
    public void checkRetryConfig() {
        var retry = retryRegistry.retry("retry-recommendations");
    }

    /**
     * URL проверки circuit breaker
     * <a href="http://127.0.0.1:8080/actuator/circuitbreakerevents/circuit">ссылка</a>
     */
    @Retry(name = "retry-recommendations")
    @CircuitBreaker(name= "circuit", fallbackMethod = "getDefaultRecommendations")
    public List<Integer> getRecommendations() {
        return Objects.requireNonNull(
                client.get()
                        .uri("/list")
                        .retrieve()
                        .onStatus(status -> status.value() == HttpStatus.BAD_REQUEST.value(),
                                (((request, response) -> {
                                    throw new BadRequestException(response.getStatusText());
                                })))
                        .onStatus(status -> status.value() == HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                ((request, response) -> {
                                    throw new InternalErrorException(response.getStatusText());
                                }))
                        .body(RecommendationsResponse.class)
        ).gamesId();
    }

    public List<Integer> getDefaultRecommendations(Throwable t) {
        if (!fallbackEnabled) {
            throw new InternalErrorException(t.getMessage());
        }
        return new ArrayList<>();
    }
}
