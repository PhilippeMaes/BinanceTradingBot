package be.crypto.bot;

import be.crypto.bot.config.Constants;
import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.*;

/**
 * Created by philippemaes on 21/10/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class VolumeByPrice {

    private static final Integer ZONES = 40;
    private Map<Integer, Double> zoneMap;

    @Autowired
    private WebService webService;

    @Test
    public void contextLoads() throws ParseException {
        zoneMap = new TreeMap<>((o1, o2) -> o2.compareTo(o1));

        List<Candlestick> candleSticks = webService.getCandleSticks(Constants.BASE, "MDA", CandlestickInterval.FIVE_MINUTES);
        Double maxClose = candleSticks.stream().mapToDouble(c -> Double.valueOf(c.getClose())).max().getAsDouble();
        Double minClose = candleSticks.stream().mapToDouble(c -> Double.valueOf(c.getClose())).min().getAsDouble();
        Double range = (maxClose - minClose) / (double) ZONES;

        Collections.sort(candleSticks, (o1, o2) -> Double.valueOf(o1.getClose()).compareTo(Double.valueOf(o2.getClose())));
        for (Candlestick candlestick : candleSticks) {
            Double close = Double.valueOf(candlestick.getClose());
            Double volume = Double.valueOf(candlestick.getVolume());
            Integer zone = (int) ((close - minClose) / range);
            zoneMap.put(zone, (zoneMap.containsKey(zone) ? zoneMap.get(zone) : 0.0) + volume);
        }

        Double average = zoneMap.values().stream().mapToDouble(v -> v.doubleValue()).sum() / ZONES;

        for (Map.Entry<Integer, Double> entry : zoneMap.entrySet()) {
            Double minPrice = minClose + (entry.getKey() * range);
            Double maxPrice = minClose + ((entry.getKey() + 1) * range);
            if (entry.getValue() > average * 1.75) System.out.println("[" + String.format("%.8f", minPrice) + " - " + String.format("%.8f", maxPrice) + "] Volume: " + entry.getValue());
        }
    }
}