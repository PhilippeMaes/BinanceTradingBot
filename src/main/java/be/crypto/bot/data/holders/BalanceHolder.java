package be.crypto.bot.data.holders;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.ClosedTradeService;
import be.crypto.bot.domain.ClosedTrade;
import be.crypto.bot.domain.OrderType;
import be.crypto.bot.service.PushOverService;
import be.crypto.bot.service.exchange.WebService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by philippemaes on 15/06/2018.
 */
@Service
public class BalanceHolder {

    private static final Logger log = LoggerFactory.getLogger(BalanceHolder.class);

    private ConcurrentHashMap<String, Double> balanceMap;
    private Double baseBalance;
    private Double baseLocked;

    @Autowired
    private ClosedTradeService closedTradeService;

    @Autowired
    private PushOverService pushOverService;

    @Autowired
    private WebService webService;

    @PostConstruct
    private void init() {
        this.balanceMap = new ConcurrentHashMap<>();
        this.baseBalance = Constants.MAX_INITIAL_BASE;
        this.baseLocked = 0.0;
    }

    public Double getBaseBalance() {
        return baseBalance;
    }

    public Double getAvailableBaseBalance() {
        return baseBalance - baseLocked;
    }

    public void setBaseBalance(Double baseBalance) {
        this.baseBalance = baseBalance;
    }

    public void lockBaseBalance(Double amount) {
        baseLocked += amount;
    }

    public void releaseBaseBalance(Double amount) {
        baseLocked -= amount;
    }

    public Double getBalance(String marketName) {
        synchronized (balanceMap) {
            return balanceMap.containsKey(marketName) ? balanceMap.get(marketName) : 0.0;
        }
    }

    public void bought(String marketName, Double quantity, Double rate) {
        balanceMap.put(marketName, getBalance(marketName) + quantity);
        baseBalance -= quantity * rate;
        log.info("[" + marketName + "] Bought " + quantity + " @ price: " + String.format("%.8f", rate) + " >> Balance: " + String.format("%.8f", getBalance(marketName)));
        closedTradeService.saveTrade(new ClosedTrade(marketName, quantity, rate, OrderType.BUY));

        try {
            pushOverService.sendFillNotification(marketName, quantity, rate, OrderType.BUY);
        } catch (IOException e) {
            log.error("Error sending buy notification through PushOver", e);
        }
    }

    public void sold(String marketName, Double quantity, Double rate) {
        balanceMap.put(marketName, getBalance(marketName) - quantity);
        baseBalance += quantity * rate;
        log.info("[" + marketName + "] Sold " + quantity + " @ price: " + String.format("%.8f", rate) + " >> BASE balance: " + String.format("%.8f", baseBalance));
        closedTradeService.saveTrade(new ClosedTrade(marketName, quantity, rate, OrderType.SELL));

        try {
            pushOverService.sendFillNotification(marketName, quantity, rate, OrderType.SELL);
        } catch (IOException e) {
            log.error("Error sending sell notification through PushOver", e);
        }
    }

    public Double getTotalBaseBalance() {
        Double totalBaseBalance = baseBalance;
        totalBaseBalance += balanceMap.entrySet()
                .stream()
                .mapToDouble(b -> Double.valueOf(webService.getTicker(b.getKey()).getLastPrice()) * b.getValue())
                .sum();
        return totalBaseBalance;
    }
}
