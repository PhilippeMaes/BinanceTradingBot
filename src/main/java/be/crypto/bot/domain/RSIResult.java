package be.crypto.bot.domain;

/**
 * Created by philippemaes on 16/06/2018.
 */
public class RSIResult {

    private double averageGain;
    private double averageLoss;
    private double RSI;

    public RSIResult(double averageGain, double averageLoss, double RSI) {
        this.averageGain = averageGain;
        this.averageLoss = averageLoss;
        this.RSI = RSI;
    }

    public double getAverageGain() {
        return averageGain;
    }

    public double getAverageLoss() {
        return averageLoss;
    }

    public double getRSI() {
        return RSI;
    }
}
