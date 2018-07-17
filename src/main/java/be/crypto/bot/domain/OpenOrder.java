package be.crypto.bot.domain;

/**
 * Created by philippemaes on 15/06/2018.
 */
public class OpenOrder {

    private String marketName;
    private Long orderId;
    private Double quantity;
    private Double rate;
    private OrderType orderType;

    public OpenOrder(String marketName, Long orderId, Double quantity, Double rate, OrderType orderType) {
        this.marketName = marketName;
        this.orderId = orderId;
        this.quantity = quantity;
        this.rate = rate;
        this.orderType = orderType;
    }

    public String getMarketName() {
        return marketName;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Double getQuantity() {
        return quantity;
    }

    public Double getRate() {
        return rate;
    }

    public OrderType getOrderType() {
        return orderType;
    }
}
