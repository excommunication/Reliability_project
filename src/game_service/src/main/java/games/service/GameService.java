package games.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {
    private final List<String> games = new ArrayList<>(List.of(
            "Standoff 2",
            "Minecraft",
            "Grand Theft Auto V",
            "Dota 2",
            "Counter-Strike 2"
    ));


    public List<String> getGamesByIds(List<Integer> idList) {
        List<String> res = new ArrayList<>();
        for (int id: idList) {
            if (id >= games.size() || id < 0) {
                throw new IndexOutOfBoundsException("Game not found");
            }
            res.add(games.get(id));
        }
        return res;
    }
}
