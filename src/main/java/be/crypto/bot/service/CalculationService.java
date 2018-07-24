package be.crypto.bot.service;

import be.crypto.bot.config.Constants;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.RSIResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by philippemaes on 15/06/2018.
 */
@Service
public class CalculationService {

    public double getEMA(int period, double previousEMA, double last) throws IllegalArgumentException {
        double K = (double) 2 / (period + 1);
        double ema = (last * K) + (previousEMA * (1 - K));

        return ema;
    }

    public RSIResult getInitialRSI(List<Double> closes, Double close) {
        if (closes.size() < Constants.RSI_PERIOD) {
            return null;
        }

        List<Double> concatenatedCloseList = new ArrayList<>();
        concatenatedCloseList.addAll(closes);
        concatenatedCloseList.add(close);

        double gain = 0.0;
        double loss = 0.0;
        for (int i = 0; i < (concatenatedCloseList.size() - 1); i++) {
            if (i == 0) continue;

            double diff = concatenatedCloseList.get(i) - concatenatedCloseList.get(i - 1);
            if (diff > 0) {
                gain += diff;
            } else {
                loss += -diff;
            }
        }

        double averageGain = gain / Constants.RSI_PERIOD;
        double averageLoss = loss / Constants.RSI_PERIOD;

        double rs = averageGain / averageLoss;
        return new RSIResult(averageGain, averageLoss, 100 - 100 / (1 + rs));
    }

    public RSIResult getWildersRSI(RSIResult previous, Double gain) {
        double averageGain = (previous.getAverageGain() * (Constants.RSI_PERIOD - 1) + (gain > 0.0 ? gain : 0.0)) / Constants.RSI_PERIOD;
        double averageLoss = (previous.getAverageLoss() * (Constants.RSI_PERIOD - 1) + (gain < 0.0 ? -gain : 0.0)) / Constants.RSI_PERIOD;

        double rs = averageGain / averageLoss;
        return new RSIResult(averageGain, averageLoss, 100 - 100 / (1 + rs));
    }

    public Double getStochRSI(List<Double> RSIs) {
        if (RSIs.size() != Constants.RSI_PERIOD) {
            return null;
        }

        Double currentRSI = RSIs.get(RSIs.size() - 1);
        Double lowestRSI = RSIs.stream().min(Comparator.comparing(Double::valueOf)).get();
        Double highestRSI = RSIs.stream().max(Comparator.comparing(Double::valueOf)).get();

        return (currentRSI - lowestRSI) / (highestRSI - lowestRSI);
    }

    public double getLowerBollingerBand(Double EMA, List<Double> priceHistory) {
        return EMA - 2 * getStandardDeviation(EMA, priceHistory);
    }

    public double getUpperBollingerBand(Double EMA, List<Double> priceHistory) {
        return EMA + 2 * getStandardDeviation(EMA, priceHistory);
    }

    public double getStandardDeviation(Double EMA, List<Double> priceHistory) {
        double deviationSum = 0.0;
        for (Double price : priceHistory) {
            double deviation = price - EMA;
            double squaredDeviation = deviation * deviation;
            deviationSum += squaredDeviation;
        }
        double deviatianAverage = deviationSum / priceHistory.size();
        double root = Math.sqrt(deviatianAverage);
        return root;
    }
}
