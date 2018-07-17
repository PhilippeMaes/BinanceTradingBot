package be.crypto.bot.domain.DTO;

/**
 * Created by philippemaes on 18/06/2018.
 */
public class TradeDTO {

    private String marketName;
    private String averagePrice;
    private String quantity;
    private String orderType;
    private String PL;
    private String date;

    public TradeDTO(String marketName, String averagePrice, String quantity, String orderType, String date) {
        this(marketName, averagePrice, quantity, orderType, null, date);
    }

    public TradeDTO(String marketName, String averagePrice, String quantity, String orderType, String PL, String date) {
        this.marketName = marketName;
        this.averagePrice = averagePrice;
        this.quantity = quantity;
        this.orderType = orderType;
        this.PL = PL;
        this.date = date;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(String averagePrice) {
        this.averagePrice = averagePrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getPL() {
        return PL;
    }

    public void setPL(String PL) {
        this.PL = PL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
