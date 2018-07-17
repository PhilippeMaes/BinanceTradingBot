package be.crypto.bot.domain;

/**
 * Created by philippemaes on 16/06/2018.
 */
public class MarketState {

    private Double SMA;
    private Double EMA;
    private RSIResult RSI;
    private Double StochRSI;

    public MarketState(Double SMA, Double EMA, RSIResult RSI, Double stochRSI) {
        this.SMA = SMA;
        this.EMA = EMA;
        this.RSI = RSI;
        StochRSI = stochRSI;
    }

    public Double getSMA() {
        return SMA;
    }

    public Double getEMA() {
        return EMA;
    }

    public Double getRSI() {
        return RSI.getRSI();
    }

    public Double getStochRSI() {
        return StochRSI;
    }
}
