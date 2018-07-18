package be.crypto.bot.rest;

import be.crypto.bot.domain.BalanceSnapshot;
import be.crypto.bot.domain.ClosedTrade;
import be.crypto.bot.domain.DTO.BalanceDTO;
import be.crypto.bot.domain.DTO.OpenPositionDTO;
import be.crypto.bot.domain.DTO.TradeDTO;
import be.crypto.bot.domain.OrderType;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by philippemaes on 18/06/2018.
 */
public class Factory {

    public static BalanceDTO fromBalances(List<BalanceSnapshot> balances) {
        List<String> dates = balances.stream().map(b -> b.getFormattedDate()).collect(Collectors.toList());
        List<Double> values = balances.stream().map(b -> b.getBalance()).collect(Collectors.toList());
        return new BalanceDTO(dates, values);
    }

    public static List<TradeDTO> fromTrades(List<ClosedTrade> closedTradeList) {
        return closedTradeList.stream().map(trade -> createTradeDTO(trade)).collect(Collectors.toList());
    }

    public static TradeDTO createTradeDTO(ClosedTrade closedTrade) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(closedTrade.getTimestamp()));
        return new TradeDTO(closedTrade.getMarketName(), String.format("%.8f", closedTrade.getAveragePrice()), closedTrade.getQuantity().toString(), closedTrade.getOrderType().equals(OrderType.BUY) ? "Buy" : "Sell", formattedDate);
    }

    public static OpenPositionDTO createOpenPositionDTO(ClosedTrade closedTrade, Double last) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(closedTrade.getTimestamp()));
        String PL = String.format("%.2f", (last / closedTrade.getAveragePrice() - 1.0) * 100.0);
        return new OpenPositionDTO(closedTrade.getMarketName(), String.format("%.8f", closedTrade.getAveragePrice()), String.format("%.8f", last), closedTrade.getQuantity().toString(), PL, formattedDate);
    }
}
