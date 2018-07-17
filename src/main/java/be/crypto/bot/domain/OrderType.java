package be.crypto.bot.domain;

/**
 * Created by philippemaes on 15/06/2018.
 */
public enum OrderType {

    BUY("buy"), SELL("sell");

    private String type;

    OrderType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
