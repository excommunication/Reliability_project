package app.adapter;

import app.dto.GamesResponse;
import app.exceptions.WebExceptions.InternalErrorException;
import app.exceptions.WebExceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
public class GameAdapter {
    private final RestClient client;


    public GameAdapter(ClientFactory clientFactory, @Value("${rest.client.base-uri-game}") String baseUri) {
        this.client = clientFactory.getRestClient(baseUri);
    }

    @Cacheable(value = "games", key="'all'")
    public List<String> getGames(List<Integer> gameIds) {
        return Objects.requireNonNull(client.get()
                        .uri(
                                uriBuilder -> uriBuilder
                                        .path("/list")
                                        .queryParam("gamesIdList", gameIds.toArray()) // передаём массив
                                        .build()
                        )
                        .retrieve()

                        .onStatus(status -> status.value() == HttpStatus.NOT_FOUND.value(),
                                (request, response) -> {
                            throw new ResourceNotFoundException(response.getStatusText());
                        })
                        .onStatus(status -> status.value() == HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                (request, response) -> {
                                    throw new InternalErrorException(response.getStatusText());
                                })
                        .body(GamesResponse.class))
                .gameTitles();
    }
}
