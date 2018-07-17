package be.crypto.bot;

import be.crypto.bot.config.RunEnvironment;
import be.crypto.bot.data.holders.BalanceHolder;
import be.crypto.bot.service.TradeService;
import com.binance.api.client.domain.market.Candlestick;
import com.opencsv.CSVReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by philippemaes on 26/06/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TradeTest {

    private final String CSV_PATH = "/Users/philippemaes/Development/Crypto/candle.csv";

    @Autowired
    private RunEnvironment runEnvironment;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private BalanceHolder balanceHolder;

    @Test
    public void contextLoads() throws IOException, ParseException {
        runEnvironment.setTestEnv(true);

        List<Candlestick> candlestickList = new ArrayList<>();

        CSVReader reader = new CSVReader(new FileReader(CSV_PATH));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            Candlestick candlestick = new Candlestick();
            candlestick.setCloseTime(Long.valueOf(nextLine[0]));
            candlestick.setHigh(nextLine[1]);
            candlestick.setLow(nextLine[2]);
            candlestick.setClose(nextLine[3]);
            candlestickList.add(candlestick);
        }

        Map<Long, List<Candlestick>> map = candlestickList.stream().collect(Collectors.groupingBy(Candlestick::getCloseTime));
        for (List<Candlestick> candles : map.values()) {

        }
    }
}
