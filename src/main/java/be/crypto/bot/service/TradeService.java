package be.crypto.bot.service;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.ConfigHolder;
import be.crypto.bot.data.holders.BalanceHolder;
import be.crypto.bot.data.holders.MarketManager;
import be.crypto.bot.data.holders.OpenPositionHolder;
import be.crypto.bot.data.holders.OrderHolder;
import be.crypto.bot.domain.CustomLogger;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.OpenOrder;
import be.crypto.bot.domain.OrderType;
import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.Order;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.exception.BinanceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by philippemaes on 15/06/2018.
 */
@Service
public class TradeService {

    private static final CustomLogger log = new CustomLogger(TradeService.class);

    private Map<String, Long> initialSellMap;
    private Set<String> tradesInProgress;
    
    @Autowired
    private ConfigHolder configHolder;

    @Autowired
    private WebService webService;

    @Autowired
    private OrderHolder orderHolder;

    @Autowired
    private BalanceHolder balanceHolder;

    @Autowired
    private OpenPositionHolder openPositionHolder;

    @Autowired
    private MarketManager stateManager;

    @PostConstruct
    private void init() {
        this.initialSellMap = new HashMap<>();
        this.tradesInProgress = Collections.synchronizedSet(new HashSet());
    }

    public void buy(String marketName, Double limit) {
        synchronized (tradesInProgress) {
            // check symbol and balance
            SymbolInfo symbolInfo = webService.getSymbolInfo(Constants.BASE, marketName);
            if (tradesInProgress.contains(marketName) || symbolInfo == null || balanceHolder.getBalance(marketName) > Double.valueOf(symbolInfo.getFilters().get(1).getMinQty()))
                return;

            // get max quantity
            Double quantity = Constants.MAX_ORDER_SIZE / limit;

            // enough BTC?
            Double availableBaseBalance = balanceHolder.getAvailableBaseBalance();
            if (availableBaseBalance < Constants.MAX_ORDER_SIZE) {
                log.info("[" + marketName + "] Not enough free BASE to buy " + quantity + " @ price " + String.format("%.8f", limit));
                return;
            }

            // lock coin -> only release when buy was not (partially) filled after period, or if sell was filled
            tradesInProgress.add(marketName);

            // place buy order at trigger price
            log.info("[" + marketName + "] Buying " + quantity + " @ price " + String.format("%.8f", limit));
            OpenOrder openOrder = webService.placeLimitBuyOrder(Constants.BASE, marketName, quantity, limit, TimeInForce.GTC);
            orderHolder.addTrade(marketName, openOrder, OrderType.BUY);
        }
    }

    public void averageDown(String marketName) {
        // cancel if no open position for market
        if (!openPositionHolder.getOpenPosition(marketName).isPresent())
            return;

        // only average down once
        if (openPositionHolder.getOpenPosition(marketName).get().isAveragedDown())
            return;

        // buy with 1% max spread
        Double limit = Double.valueOf(stateManager.getTicker(marketName).get().getAsk()) * 1.01;
        Double quantity = Constants.AVERAGING_DOWN_BALANCE / limit;
        OpenOrder openOrder = webService.placeLimitBuyOrder(Constants.BASE, marketName, quantity, limit, TimeInForce.IOC);

        // wait for Binance to process order
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error("Error waiting for Binance", e);
        }

        // check executed amount and price
        Order order = webService.getOrder(Constants.BASE, marketName, openOrder.getOrderId());
        Double executedQty = Double.valueOf(order.getExecutedQty());
        Double executedRate = Double.valueOf(order.getPrice());

        // update balances
        if (executedQty > 0.0) {
            balanceHolder.bought(marketName, executedQty, executedRate);

            // remember we averaged down
            openPositionHolder.getOpenPosition(marketName).get().setAveragedDown(true);
        }
    }

    public void placeSell(String marketName, Double limit, boolean initial) {
        // get quantity
        Double quantity = balanceHolder.getBalance(marketName);

        // get symbol info
        SymbolInfo symbolInfo = webService.getSymbolInfo(Constants.BASE, marketName);
        Double minNotional = Double.valueOf(symbolInfo.getFilters().get(2).getMinNotional());
        if (quantity * limit < minNotional)
            return;

        // placing immediate or cancel limit order
        if (initial) log.info("[" + marketName + "] Placing sell order for " + quantity + " @ price " + String.format("%.8f", limit));
        OpenOrder openOrder = webService.placeLimitSellOrder(Constants.BASE, marketName, quantity, limit);
        if (initial) log.info("[" + marketName + "] Placed sell order for " + quantity + " @ price " + String.format("%.8f", limit));

        // remember
        orderHolder.addTrade(marketName, openOrder, OrderType.SELL);
        if (initial)
            initialSellMap.put(marketName, System.currentTimeMillis());
    }

    public void checkBuyOrders() {
        synchronized (tradesInProgress) {
            Map<String, OpenOrder> openBuyOrders = new HashMap<>(orderHolder.getOpenBuyTrades());
            log.debug("Checking [" + openBuyOrders.size() + "] buy orders");
            for (Map.Entry<String, OpenOrder> openBuyOrder : openBuyOrders.entrySet()) {
                String marketName = openBuyOrder.getKey();
                OpenOrder openOrder = openBuyOrder.getValue();

                // get order status
                Order order;
                try {
                    order = webService.getOrder(Constants.BASE, marketName, openOrder.getOrderId());
                } catch (BinanceApiException ex) {
                    log.error("Error cancelling sell order for " + marketName, ex);
                    continue;
                }
                Double executedQty = Double.valueOf(order.getExecutedQty());
                Double executedRate = Double.valueOf(order.getPrice());

                // cancel remaining order if not filled
                if (!order.getStatus().equals(OrderStatus.FILLED))
                    webService.cancelOrder(Constants.BASE, marketName, openOrder.getOrderId());
                orderHolder.removeTrade(marketName, OrderType.BUY);

                // update balances
                if (executedQty > 0.0) {
                    balanceHolder.bought(marketName, executedQty, executedRate);
                    openPositionHolder.addPosition(marketName, executedRate);

                    // place sell order
                    Optional<MarketState> marketState = stateManager.getMarketState(marketName);
                    if (marketState.isPresent())
                        placeSell(marketName, marketState.get().getSMA() * (stateManager.getAverageGap() + configHolder.getSellPercentageTrigger()), true);
                } else {
                    // release coin
                    tradesInProgress.remove(marketName);
                }
            }
        }
    }

    public void checkSellOrders() {
        synchronized (tradesInProgress) {
            Map<String, OpenOrder> openSellOrders = new HashMap<>(orderHolder.getOpenSellTrades());
            log.debug("Moving [" + openSellOrders.size() + "] sell orders");
            for (Map.Entry<String, OpenOrder> openSellOrder : openSellOrders.entrySet()) {
                // get values
                String market = openSellOrder.getKey();
                Long orderID = openSellOrder.getValue().getOrderId();

                // update balances
                Order order;
                try {
                    order = webService.getOrder(Constants.BASE, market, orderID);
                } catch (BinanceApiException ex) {
                    log.error("Error getting sell order for " + market, ex);
                    continue;
                }
                Double executedQty = Double.valueOf(order.getExecutedQty());
                if (executedQty > 0.0)
                    balanceHolder.sold(market, executedQty, Double.valueOf(order.getPrice()));

                // if not filled -> move order
                if (!order.getStatus().equals(OrderStatus.FILLED)) {
                    try {
                        webService.cancelOrder(Constants.BASE, market, orderID);
                    } catch (BinanceApiException ex) {
                        log.error("Error cancelling sell order for " + market, ex);
                        continue;
                    }
                    orderHolder.removeTrade(market, OrderType.SELL);

                    // wait for Binance to process order
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        log.error("Error waiting for Binance", e);
                    }

                    // move order
                    Optional<MarketState> marketState = stateManager.getMarketState(market);
                    if (marketState.isPresent())
                        placeSell(market, marketState.get().getSMA() * (stateManager.getAverageGap() + configHolder.getSellPercentageTrigger()), false);
                } else {
                    // release coin
                    tradesInProgress.remove(market);
                    orderHolder.removeTrade(market, OrderType.SELL);
                    openPositionHolder.removePosition(market);
                }
            }
        }
    }
}
