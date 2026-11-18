package games.web;

import games.dto.GamesRequest;
import games.dto.GamesResponse;
import games.exceptions.InternalErrorException;
import games.service.GameService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ThreadLocalRandom;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/games")
public class GamesController {
    private final GameService service;

    private int requestCounter = 0;

    @GetMapping("/list")
    public GamesResponse getGames(@ModelAttribute GamesRequest request){
        requestCounter++;
        if (requestCounter > 1) {
            throw new InternalErrorException("Internal error");
        }
        return new GamesResponse(
                service.getGamesByIds(
                        request.gamesIdList()
                )
        );
    }
}
