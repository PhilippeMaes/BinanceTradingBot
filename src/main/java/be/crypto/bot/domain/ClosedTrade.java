package be.crypto.bot.domain;

import javax.persistence.*;

/**
 * Created by philippemaes on 07/02/2018.
 */
@Entity
@Table(name = "CLOSED_TRADE")
public class ClosedTrade {

    @Id
    @GeneratedValue
    private Long id;

    private String marketName;
    private Double averagePrice;
    private Double quantity;
    private OrderType orderType;
    private Long timestamp;

    private ClosedTrade() {
    }

    public ClosedTrade(String marketName, Double quantity, Double averagePrice, OrderType orderType) {
        this(marketName, quantity, averagePrice, orderType, System.currentTimeMillis());
    }

    public ClosedTrade(String marketName, Double quantity, Double averagePrice, OrderType orderType, Long timestamp) {
        this.marketName = marketName;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
        this.orderType = orderType;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getMarketName() {
        return marketName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getAveragePrice() {
        return averagePrice;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
