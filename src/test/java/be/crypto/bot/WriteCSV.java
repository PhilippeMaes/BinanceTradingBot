package be.crypto.bot;

import be.crypto.bot.config.Constants;
import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by philippemaes on 26/06/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class WriteCSV {

    private static final char DEFAULT_SEPARATOR = ',';

    @Autowired
    private WebService webService;

    @Test
    public void contextLoads() throws ParseException, IOException {
        List<TickerPrice> tickers = webService.getTickers();
        List<Candlestick> candlesticks = new ArrayList<>();
        for (String market : tickers.stream().filter(t -> t.getSymbol().endsWith(Constants.BASE)).map(t -> t.getSymbol().substring(0, t.getSymbol().length() - 3)).collect(Collectors.toList())) {
            candlesticks.addAll(webService.getCandleSticks(Constants.BASE, market, CandlestickInterval.FIVE_MINUTES));
        }
        List<Candlestick> sorted = candlesticks.stream().sorted((c1, c2) -> c1.getCloseTime().compareTo(c2.getCloseTime())).collect(Collectors.toList());

        String csvFile = "/Users/philippemaes/Development/Crypto/candle.csv";
        FileWriter writer = new FileWriter(csvFile);
        for (Candlestick candlestick : sorted) {
            writeLine(writer, Arrays.asList(candlestick.getCloseTime().toString(), candlestick.getHigh().toString(), candlestick.getLow().toString(), candlestick.getClose().toString()));
        }

        writer.flush();
        writer.close();
    }

    public static void writeLine(Writer w, List<String> values) throws IOException {
        writeLine(w, values, DEFAULT_SEPARATOR, ' ');
    }

    public static void writeLine(Writer w, List<String> values, char separators) throws IOException {
        writeLine(w, values, separators, ' ');
    }

    //https://tools.ietf.org/html/rfc4180
    private static String followCVSformat(String value) {
        String result = value;
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        return result;
    }

    public static void writeLine(Writer w, List<String> values, char separators, char customQuote) throws IOException {
        boolean first = true;

        //default customQuote is empty
        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }

        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            if (!first) {
                sb.append(separators);
            }
            if (customQuote == ' ') {
                sb.append(followCVSformat(value));
            } else {
                sb.append(customQuote).append(followCVSformat(value)).append(customQuote);
            }

            first = false;
        }
        sb.append("\n");
        w.append(sb.toString());
    }

}
