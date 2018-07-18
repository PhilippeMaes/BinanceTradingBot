package be.crypto.bot.data;

import be.crypto.bot.domain.OrderType;
import be.crypto.bot.domain.ClosedTrade;
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

    public void saveTrade(ClosedTrade closedTrade) {
        closedTradeRepository.save(closedTrade);
    }

    public List<ClosedTrade> getTrades() {
        List<ClosedTrade> combinedClosedTrades = new ArrayList<>();
        Map<String, List<ClosedTrade>> tradesByMarketName = closedTradeRepository.findAll().stream().collect(Collectors.groupingBy(ClosedTrade::getMarketName));

        for (Map.Entry<String, List<ClosedTrade>> trades : tradesByMarketName.entrySet()) {
            if (trades.getValue().size() == 0)
                continue;

            List<ClosedTrade> combinedTradesForMarket = new ArrayList<>();

            Iterator<ClosedTrade> iterator = trades.getValue().iterator();
            ClosedTrade nextClosedTrade = iterator.next();
            OrderType orderType = nextClosedTrade.getOrderType();
            Double qty = nextClosedTrade.getQuantity();
            Double btc = nextClosedTrade.getQuantity() * nextClosedTrade.getAveragePrice();
            Long timestamp = nextClosedTrade.getTimestamp();
            while (iterator.hasNext()) {
                nextClosedTrade = iterator.next();
                if (!nextClosedTrade.getOrderType().equals(orderType)) {
                    combinedTradesForMarket.add(new ClosedTrade(trades.getKey(), qty, btc / qty, orderType, timestamp));
                    orderType = nextClosedTrade.getOrderType();
                    qty = nextClosedTrade.getQuantity();
                    btc = nextClosedTrade.getQuantity() * nextClosedTrade.getAveragePrice();
                    timestamp = nextClosedTrade.getTimestamp();
                } else {
                    qty += nextClosedTrade.getQuantity();
                    btc += nextClosedTrade.getQuantity() * nextClosedTrade.getAveragePrice();
                    timestamp = nextClosedTrade.getTimestamp();
                }
            }
            combinedTradesForMarket.add(new ClosedTrade(trades.getKey(), qty, btc / qty, orderType, timestamp));
            combinedClosedTrades.addAll(combinedTradesForMarket);
        }

        return combinedClosedTrades.stream().sorted(Comparator.comparing(ClosedTrade::getTimestamp).reversed()).collect(Collectors.toList());
    }

    public List<ClosedTrade> getOpenPositions() {
        Map<String, ClosedTrade> lastTradeForMarket = new HashMap<>();
        for (ClosedTrade closedTrade : getTrades()) {
            if (!lastTradeForMarket.containsKey(closedTrade.getMarketName()))
                lastTradeForMarket.put(closedTrade.getMarketName(), closedTrade);
        }
        return lastTradeForMarket.values().stream().filter(t -> t.getOrderType().equals(OrderType.BUY)).collect(Collectors.toList());
    }
}
