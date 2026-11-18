package recomendations.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recomendations.dto.RecommendationsResponse;
import recomendations.exceptions.InternalErrorException;
import recomendations.exceptions.BadRequestException;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    @GetMapping("/list")
    public RecommendationsResponse getRecommendations() {
        int currentCount = requestCounter.getAndIncrement();

        if (currentCount % 3 == 0) {
            throw new InternalErrorException("Internal Error");
        } else if (currentCount % 3 == 2) {
            throw new BadRequestException("Bad Request");
        };
        return new RecommendationsResponse(
                IntStream.rangeClosed(0, 4)
                .boxed()
                .toList()
        );
    }
}
