package be.crypto.bot.data.holders;

import be.crypto.bot.domain.OpenOrder;
import be.crypto.bot.domain.OrderType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by philippemaes on 15/06/2018.
 */
@Service
public class OrderHolder {

    private Map<String, OpenOrder> openBuyTrades;
    private Map<String, OpenOrder> openSellTrades;

    @Autowired
    private BalanceHolder balanceHolder;

    @PostConstruct
    private void init() {
        this.openBuyTrades = new ConcurrentHashMap<>();
        this.openSellTrades = new ConcurrentHashMap<>();
    }

    public void addTrade(String marketName, OpenOrder openOrder, OrderType orderType) {
        switch (orderType) {
            case BUY:
                // lock BASE balance
                balanceHolder.lockBaseBalance(openOrder.getQuantity() * openOrder.getRate());

                openBuyTrades.put(marketName, openOrder);
                break;
            case SELL:
                openSellTrades.put(marketName, openOrder);
                break;
        }
    }

    public void removeTrade(String marketName, OrderType orderType) {
        switch (orderType) {
            case BUY:
                // release BASE balance
                OpenOrder openOrder = openBuyTrades.get(marketName);
                if (openOrder != null)
                    balanceHolder.releaseBaseBalance(openOrder.getQuantity() * openOrder.getRate());

                openBuyTrades.remove(marketName);
                break;
            case SELL:
                openSellTrades.remove(marketName);
                break;
        }
    }

    public OpenOrder getTrade(String marketName, OrderType orderType) {
        switch (orderType) {
            case BUY:
                return openBuyTrades.get(marketName);
            case SELL:
                return openSellTrades.get(marketName);
        }
        return null;
    }

    public Map<String, OpenOrder> getOpenBuyTrades() {
        return this.openBuyTrades;
    }

    public Map<String, OpenOrder> getOpenSellTrades() {
        return this.openSellTrades;
    }
}
