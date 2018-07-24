package be.crypto.bot.data.holders;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.ConfigHolder;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.MarketTicker;
import be.crypto.bot.service.AnalyseService;
import be.crypto.bot.service.CalculationService;
import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by philippemaes on 15/06/2018.
 */
@Service
public class MarketStateManager {

    private static final Logger log = LoggerFactory.getLogger(MarketStateManager.class);

    private Map<String, MarketStateHolder> holders;
    private Map<String, MarketState> marketStates;
    private Map<String, MarketTicker> marketTickers;
    private Double averageGap;

    @Autowired
    private ConfigHolder configHolder;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private AnalyseService analyseService;

    @Autowired
    private WebService webService;

    public MarketStateManager() {
        this.holders = new HashMap<>();
        this.marketStates = new HashMap<>();
        this.marketTickers = new HashMap<>();
    }

    public void collectMarketStates() {
        List<Double> averageGapList = new ArrayList<>();
        for (Map.Entry<String, MarketStateHolder> holder : holders.entrySet()) {
            if (!marketTickers.containsKey(holder.getKey()))
                continue;

            Double close = Double.valueOf(marketTickers.get(holder.getKey()).getClose());
            MarketState marketState = holder.getValue().getMarketState(close);

            averageGapList.add(close / marketState.getEMA());

            marketStates.put(holder.getKey(), marketState);
        }
        averageGap = averageGapList.stream().mapToDouble(Double::doubleValue).sum() / averageGapList.size();
        log.info("Average market gap: " + averageGap);
    }

    public Optional<MarketTicker> getTicker(String market) {
        return marketTickers.containsKey(market) ? Optional.of(marketTickers.get(market)) : Optional.empty();
    }

    public Double getAverageGap() {
        return averageGap;
    }

    public void updateTicker(String market, MarketTicker marketTicker) {
        if (!holders.containsKey(market)) {
            Optional<MarketState> marketState = initMarketState(market, Double.valueOf(marketTicker.getClose()));
            if (marketState.isPresent())
                marketStates.put(market, marketState.get());
            else
                return;
        }

        marketTickers.put(market, marketTicker);

        if (marketStates.containsKey(market))
            analyseService.checkTicker(marketStates.get(market), marketTicker);
    }

    public Optional<MarketState> getMarketState(String market) {
        return marketStates.containsKey(market) ? Optional.of(marketStates.get(market)) : Optional.empty();
    }

    private Optional<MarketState> initMarketState(String market, Double close) {
        Integer size = Math.max(configHolder.getSMALength(), Constants.RSI_PERIOD * 2 + 1);
        List<Candlestick> candleSticks = webService.getCandleSticks(Constants.BASE, market, CandlestickInterval.FIVE_MINUTES);
        if (candleSticks.size() < size)
            return Optional.empty();

        List<Double> closes = candleSticks.subList(candleSticks.size() - size, candleSticks.size()).stream().map(c -> Double.valueOf(c.getClose())).collect(Collectors.toList());
        MarketStateHolder holder = new MarketStateHolder(closes, calculationService, configHolder);
        holders.put(market, holder);
        return Optional.of(holder.getMarketState(close));
    }
}
