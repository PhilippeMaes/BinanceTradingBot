package be.crypto.bot.service;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.ConfigHolder;
import be.crypto.bot.data.holders.MarketStateManager;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.MarketTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Created by philippemaes on 18/06/2018.
 */
@Service
public class AnalyseService {

    private static final Logger log = LoggerFactory.getLogger(AnalyseService.class);

    @Autowired
    private ConfigHolder configHolder;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private MarketStateManager marketStateManager;

    public void checkTicker(MarketState marketState, MarketTicker marketTicker) {
        if (Arrays.asList(Constants.BLACK_LIST).contains(marketTicker.getSymbol()) || marketStateManager.getAverageGap() == null)
            return;

        // get variables
        String market = marketTicker.getSymbol();
        Double bid = Double.valueOf(marketTicker.getBid());
        Double trigger = marketState.getSMA() * (marketStateManager.getAverageGap() - configHolder.getBuyPercentageTrigger());

        // analyse
        if (bid <= trigger)
            tradeService.buy(market, trigger);
    }
}
