package be.crypto.bot.rest;

import be.crypto.bot.domain.DTO.OpenPositionDTO;
import be.crypto.bot.domain.DTO.TradeDTO;
import be.crypto.bot.domain.OrderType;
import be.crypto.bot.domain.Trade;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by philippemaes on 18/06/2018.
 */
public class Factory {

    public static List<TradeDTO> fromTrades(List<Trade> tradeList) {
        return tradeList.stream().map(trade -> createTradeDTO(trade)).collect(Collectors.toList());
    }

    public static TradeDTO createTradeDTO(Trade trade) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(trade.getTimestamp()));
        return new TradeDTO(trade.getMarketName(), String.format("%.8f", trade.getAveragePrice()), trade.getQuantity().toString(), trade.getOrderType().equals(OrderType.BUY) ? "Buy" : "Sell", formattedDate);
    }

    public static OpenPositionDTO createOpenPositionDTO(Trade trade, Double last) {
        String formattedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(trade.getTimestamp()));
        String PL = String.format("%.2f", (last / trade.getAveragePrice() - 1.0) * 100.0);
        return new OpenPositionDTO(trade.getMarketName(), String.format("%.8f", trade.getAveragePrice()), String.format("%.8f", last), trade.getQuantity().toString(), PL, formattedDate);
    }
}
