package be.crypto.bot;

import be.crypto.bot.config.RunEnvironment;
import be.crypto.bot.data.holders.BalanceHolder;
import be.crypto.bot.data.holders.MarketStateManager;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.MarketTicker;
import be.crypto.bot.service.AnalyseService;
import be.crypto.bot.service.ThreadService;
import be.crypto.bot.service.TradeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.PostConstruct;
import java.text.ParseException;

/**
 * Created by philippemaes on 25/06/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestTradeService {

    private static final Logger log = LoggerFactory.getLogger(TestTradeService.class);
    private static final String marketName = "AST";

    private MarketState marketState;
    private MarketTicker marketTicker;

    @Autowired
    private AnalyseService analyseService;

    @Autowired
    private BalanceHolder balanceHolder;

    @Autowired
    private ThreadService threadService;

    @Autowired
    private RunEnvironment runEnvironment;

    @Autowired
    private MarketStateManager marketStateManager;

    @PostConstruct
    private void init() {
        this.runEnvironment.setTestEnv(true);
        this.marketState = new MarketState(0.10, 0.10, null, null);
        this.marketTicker = new MarketTicker();
        marketTicker.setSymbol(marketName);
        marketTicker.setAsk("0.0055");
        marketTicker.setBid("0.0055");
        marketTicker.setClose("0.0055");
    }

    @Test
    public void contextLoads() throws InterruptedException {
        marketStateManager.updateTicker(marketName, marketTicker);

        Thread.sleep(1000);

        marketStateManager.updateTicker(marketName, marketTicker);

        Thread.sleep(1000);

        marketStateManager.updateTicker(marketName, marketTicker);

//        threadService.addTask(() ->
//            analyseService.checkTicker(marketState, marketTicker, 1.0)
//        );
//
//        Thread.sleep(1000);
//
//        threadService.addTask(() ->
//                analyseService.checkTicker(marketState, marketTicker, 1.0)
//        );
//
//        Thread.sleep(1000);
//
//        threadService.addTask(() ->
//                analyseService.checkTicker(marketState, marketTicker, 1.0)
//        );
//
//        Thread.sleep(10000);
    }
}
