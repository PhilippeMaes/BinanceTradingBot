package be.crypto.bot;

import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by philippemaes on 02/10/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RenkoTest {

    private static final String BASE = "BTC";
    private static final String MARKET = "BLZ";
    private static final Double START_BASE = 1000.0;

    private Double lastBlockPrice;
    private Double ATR;
    private List<Candlestick> range = new ArrayList<>();

    @Autowired
    private WebService webService;

    @Test
    public void contextLoads() {
        double base = START_BASE;
        double quantity = 0.0;
        Double close = 0.0;

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");

        List<Candlestick> candleSticks = webService.getCandleSticks(BASE, MARKET, CandlestickInterval.HOURLY);
        for (Candlestick candlestick : candleSticks) {
            String dateTime = df.format(new Date(candlestick.getCloseTime()));
            close = Double.valueOf(candlestick.getClose());

            range.add(candlestick);
            if (range.size() < 14)
                continue;

            if (range.size() > 14)
                range.remove(0);

            if (lastBlockPrice == null)
                lastBlockPrice = Double.valueOf(candlestick.getClose());

            ATR = range.stream().mapToDouble(c -> Double.valueOf(c.getHigh())).max().getAsDouble() - range.stream().mapToDouble(c -> Double.valueOf(c.getLow())).min().getAsDouble();

            double currentPrice = Double.valueOf(candlestick.getClose());
            int blocks = (int) ((currentPrice - lastBlockPrice) / ATR);

            if (blocks == 0)
                continue;

            lastBlockPrice = lastBlockPrice + ATR * blocks;

            if (quantity > 0.0 && blocks < 0) {
                System.out.println("SOLD @ " + close + " | " + dateTime);
                base = close * quantity;
                quantity = 0.0;
            } else if (quantity == 0.0 && blocks > 0) {
                System.out.println("BOUGHT @ " + close + " | " + dateTime);
                quantity = base / close;
                base = 0.0;
            }
        }


        if (quantity > 0.0) {
            System.out.println("SOLD @ " + close);
            base = close * quantity;
        }

        System.out.println("-- RESULT: " + base);

        double hodl = START_BASE * (Double.valueOf(candleSticks.get(candleSticks.size() - 1).getClose()) / Double.valueOf(candleSticks.get(0).getClose()));
        System.out.println("-- HODL: " + hodl);

    }
}
