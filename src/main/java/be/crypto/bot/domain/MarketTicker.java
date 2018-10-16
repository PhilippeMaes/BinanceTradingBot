package be.crypto.bot.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "s",
        "b",
        "B",
        "a",
        "A",
})
public class MarketTicker {

    @JsonProperty("c")
    private String close;
    @JsonProperty("s")
    private String symbol;
    @JsonProperty("b")
    private String bid;
    @JsonProperty("B")
    private String bidQty;
    @JsonProperty("a")
    private String ask;
    @JsonProperty("A")
    private String askQty;
    @JsonProperty("v")
    private String volume;

    @JsonProperty("c")
    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }

    @JsonProperty("s")
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @JsonProperty("b")
    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    @JsonProperty("B")
    public String getBidQty() {
        return bidQty;
    }

    @JsonProperty("a")
    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }

    @JsonProperty("A")
    public String getAskQty() {
        return askQty;
    }

    @JsonProperty("v")
    public String getVolume() {
        return volume;
    }
}