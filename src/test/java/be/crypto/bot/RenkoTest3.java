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
import java.util.Date;
import java.util.List;

/**
 * Created by philippemaes on 02/10/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RenkoTest3 {

    private static final String BASE = "USDT";
    private static final String MARKET = "TRX";
    private static final Double PERC = 0.03;
    private static final Double START_BASE = 1000.0;

    private Double lastPrice;

    @Autowired
    private WebService webService;

    @Test
    public void contextLoads() {
        double base = START_BASE;
        double quantity = 0.0;

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");

        List<Candlestick> candleSticks = webService.getCandleSticks(BASE, MARKET, CandlestickInterval.HOURLY);
        for (Candlestick candlestick : candleSticks) {
            if (lastPrice == null) {
                lastPrice = Double.valueOf(candlestick.getClose());
                continue;
            }

            double currentPrice = Double.valueOf(candlestick.getClose());
            double percentageDiff = currentPrice / lastPrice;
            int blocks = (int) (Math.abs(percentageDiff - 1.0) / PERC);

            if (blocks == 0)
                continue;

            String dateTime = df.format(new Date(candlestick.getCloseTime()));

            if (quantity > 0.0 && currentPrice < lastPrice) {
                System.out.println("SOLD @ " + lastPrice + " | " + dateTime);
                base = lastPrice * quantity;
                quantity = 0.0;
            } else if (quantity == 0.0 && lastPrice > currentPrice) {
                System.out.println("BOUGHT @ " + lastPrice + " | " + dateTime);
                quantity = base / lastPrice;
                base = 0.0;
            }

            lastPrice = Double.valueOf(candlestick.getClose());
        }

        if (quantity > 0.0) {
            System.out.println("SOLD @ " + lastPrice);
            base = lastPrice * quantity;
        }

        System.out.println("-- RESULT: " + base);

        double hodl = START_BASE * (Double.valueOf(candleSticks.get(candleSticks.size() - 1).getClose()) / Double.valueOf(candleSticks.get(0).getClose()));
        System.out.println("-- HODL: " + hodl);

    }
}
