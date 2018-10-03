package be.crypto.bot.data.holders;

import be.crypto.bot.domain.OpenPosition;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by philippemaes on 03/10/2018.
 */
@Service
public class OpenPositionHolder {

    private Map<String, OpenPosition> positionMap;

    @PostConstruct
    private void init() {
        this.positionMap = new HashMap<>();
    }

    public Optional<OpenPosition> getOpenPosition(String marketName) {
        return positionMap.containsKey(marketName) ? Optional.of(positionMap.get(marketName)) : Optional.empty();
    }

    public void addPosition(String marketName, Double entry) {
        positionMap.put(marketName, new OpenPosition(marketName, entry));
    }

    public void removePosition(String marketName) {
        positionMap.remove(marketName);
    }
}
