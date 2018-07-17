package be.crypto.bot.jobs;

import be.crypto.bot.data.holders.MarketStateManager;
import be.crypto.bot.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by philippemaes on 18/06/2018.
 */
@Service
public class UpdateMarketStateJob {

    @Autowired
    private MarketStateManager stateManager;

    @Autowired
    private TradeService tradeService;

    @Scheduled(cron = "0 0/5 * * * ?")
    public void execute() {
        // first collect new market states
        stateManager.collectMarketStates();

        // next, move open sell orders
        tradeService.checkSellOrders();

        // last, check buy orders
        tradeService.checkBuyOrders();
    }
}
