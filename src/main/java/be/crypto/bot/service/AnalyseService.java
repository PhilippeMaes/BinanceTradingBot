package be.crypto.bot.service;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.ConfigHolder;
import be.crypto.bot.data.holders.MarketManager;
import be.crypto.bot.data.holders.OpenPositionHolder;
import be.crypto.bot.domain.CustomLogger;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.MarketTicker;
import be.crypto.bot.domain.OpenPosition;
import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Created by philippemaes on 18/06/2018.
 */
@Service
public class AnalyseService {

    private static final CustomLogger log = new CustomLogger(AnalyseService.class);

    @Autowired
    private ConfigHolder configHolder;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private MarketManager marketManager;

    @Autowired
    private OpenPositionHolder openPositionHolder;

    @Autowired
    private WebService webService;

    public void checkTicker(MarketState marketState, MarketTicker marketTicker) {
        String market = marketTicker.getSymbol();

        if (Arrays.asList(Constants.BLACK_LIST).contains(market) || marketManager.getAverageGap() == null || marketManager.getAverageRSI() == null)
            return;

        // we don't want to buy in a market wide drop
        if (marketManager.getAverageRSI() < 45.0)
            return;

        // market price needs to be at least 100 sats
        if (Double.valueOf(marketManager.getTicker(market).get().getClose()) < 0.00000100)
            return;

        // get variables
        Double bid = Double.valueOf(marketTicker.getBid());
        Double trigger = marketState.getSMA() * (marketManager.getAverageGap() - configHolder.getBuyPercentageTrigger());

        // analyse
        if (bid <= trigger) {
            // we don't want to buy when market crashes too quick and with high volume, a.k.a. a big dump
            Double averageVolumeFiveMin = Double.valueOf(marketManager.getTicker(market).get().getVolume()) / 288.0;
            List<Candlestick> candleSticks = webService.getCandleSticks(Constants.BASE, market, CandlestickInterval.FIVE_MINUTES);
            Candlestick latestClosedCandle = candleSticks.get(candleSticks.size() - 2);
            Candlestick currentCandle = candleSticks.get(candleSticks.size() - 1);
            if (Double.valueOf(latestClosedCandle.getVolume()) / averageVolumeFiveMin > 25.0 || Double.valueOf(currentCandle.getVolume()) / averageVolumeFiveMin > 25.0) {
                log.info("[" + market + "] Not buying because market is dumping too hard");
                return;
            }

            tradeService.buy(market, trigger);
        }

        // average down if needed
        Optional<OpenPosition> openPosition = openPositionHolder.getOpenPosition(market);
        if (openPosition.isPresent() && Double.valueOf(marketTicker.getClose()) < openPosition.get().getEntry() * (1.0 - Constants.AVERAGING_DOWN_PERC_TRIGGER))
            tradeService.averageDown(market);
    }
}
