package be.crypto.bot.data;

import be.crypto.bot.domain.OrderType;
import be.crypto.bot.domain.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by philippemaes on 07/02/2018.
 */
@Service
public class ClosedTradeService {

    @Autowired
    private ClosedTradeRepository closedTradeRepository;

    public void saveTrade(Trade trade) {
        closedTradeRepository.save(trade);
    }

    public List<Trade> getTrades() {
        List<Trade> combinedTrades = new ArrayList<>();
        Map<String, List<Trade>> tradesByMarketName = closedTradeRepository.findAll().stream().collect(Collectors.groupingBy(Trade::getMarketName));

        for (Map.Entry<String, List<Trade>> trades : tradesByMarketName.entrySet()) {
            if (trades.getValue().size() == 0)
                continue;

            List<Trade> combinedTradesForMarket = new ArrayList<>();

            Iterator<Trade> iterator = trades.getValue().iterator();
            Trade nextTrade = iterator.next();
            OrderType orderType = nextTrade.getOrderType();
            Double qty = nextTrade.getQuantity();
            Double btc = nextTrade.getQuantity() * nextTrade.getAveragePrice();
            Long timestamp = nextTrade.getTimestamp();
            while (iterator.hasNext()) {
                nextTrade = iterator.next();
                if (!nextTrade.getOrderType().equals(orderType)) {
                    combinedTradesForMarket.add(new Trade(trades.getKey(), qty, btc / qty, orderType, timestamp));
                    orderType = nextTrade.getOrderType();
                    qty = nextTrade.getQuantity();
                    btc = nextTrade.getQuantity() * nextTrade.getAveragePrice();
                    timestamp = nextTrade.getTimestamp();
                } else {
                    qty += nextTrade.getQuantity();
                    btc += nextTrade.getQuantity() * nextTrade.getAveragePrice();
                    timestamp = nextTrade.getTimestamp();
                }
            }
            combinedTradesForMarket.add(new Trade(trades.getKey(), qty, btc / qty, orderType, timestamp));
            combinedTrades.addAll(combinedTradesForMarket);
        }

        return combinedTrades.stream().sorted(Comparator.comparing(Trade::getTimestamp).reversed()).collect(Collectors.toList());
    }

    public List<Trade> getOpenPositions() {
        Map<String, Trade> lastTradeForMarket = new HashMap<>();
        for (Trade trade : getTrades()) {
            if (!lastTradeForMarket.containsKey(trade.getMarketName()))
                lastTradeForMarket.put(trade.getMarketName(), trade);
        }
        return lastTradeForMarket.values().stream().filter(t -> t.getOrderType().equals(OrderType.BUY)).collect(Collectors.toList());
    }
}
