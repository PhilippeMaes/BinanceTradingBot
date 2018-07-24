package be.crypto.bot.data.holders;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.ConfigHolder;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.RSIResult;
import be.crypto.bot.service.CalculationService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by philippemaes on 15/06/2018.
 */
public class MarketStateHolder {

    private CalculationService calculationService;
    private ConfigHolder configHolder;

    private List<Double> closeHolder;
    private Double previousClose;
    private Double EMA;
    private Double SMA;
    private Double StochRSI;
    private RSIResult WildersRSI;
    private Double lowerBollinger;
    private Double upperBollinger;
    private List<Double> RSIHolder;

    public MarketStateHolder(List<Double> closes, CalculationService calculationService, ConfigHolder configHolder) {
        this.calculationService = calculationService;
        this.configHolder = configHolder;
        this.closeHolder = new ArrayList<>();
        this.closeHolder.addAll(closes);
        this.RSIHolder = new ArrayList<>();

        calculateInitialMarketState();
    }

    public MarketState getMarketState(Double close) {
        closeHolder.add(close);
        closeHolder = closeHolder.subList(closeHolder.size() - configHolder.getSMALength(), closeHolder.size());
        SMA = closeHolder.stream().mapToDouble(Double::doubleValue).sum() / configHolder.getSMALength();
        lowerBollinger = calculationService.getLowerBollingerBand(SMA, closeHolder.subList(closeHolder.size() - Constants.BOLLINGER_PERIOD, closeHolder.size()));
        upperBollinger = calculationService.getUpperBollingerBand(SMA, closeHolder.subList(closeHolder.size() - Constants.BOLLINGER_PERIOD, closeHolder.size()));

        EMA = calculationService.getEMA(configHolder.getSMALength(), EMA, close);
        double gain = close - previousClose;
        WildersRSI = calculationService.getWildersRSI(WildersRSI, gain);
        RSIHolder.add(WildersRSI.getRSI());
        RSIHolder.remove(0);
        StochRSI = calculationService.getStochRSI(RSIHolder.subList(RSIHolder.size() - Constants.RSI_PERIOD, RSIHolder.size()));
        previousClose = close;

        return new MarketState(SMA, EMA, WildersRSI, StochRSI, lowerBollinger, upperBollinger);
    }

    public void calculateInitialMarketState() {
        int size = closeHolder.size();
        EMA = closeHolder.subList(closeHolder.size() - configHolder.getSMALength(), closeHolder.size()).stream().mapToDouble(Double::doubleValue).sum() / configHolder.getSMALength();
        SMA = EMA;

        WildersRSI = calculationService.getInitialRSI(closeHolder.subList(size - Constants.RSI_PERIOD * 2, size - Constants.RSI_PERIOD), closeHolder.get(closeHolder.size() - Constants.RSI_PERIOD));
        RSIHolder.add(WildersRSI.getRSI());
        for (int i = 0; i < Constants.RSI_PERIOD - 1; i++) {
            double gain = closeHolder.get(closeHolder.size() - Constants.RSI_PERIOD + i + 1) - closeHolder.get(closeHolder.size() - Constants.RSI_PERIOD + i);
            WildersRSI = calculationService.getWildersRSI(WildersRSI, gain);
            RSIHolder.add(WildersRSI.getRSI());
        }

        StochRSI = calculationService.getStochRSI(RSIHolder.subList(RSIHolder.size() - Constants.RSI_PERIOD, RSIHolder.size()));

        previousClose = closeHolder.get(closeHolder.size() - 1);
        closeHolder = closeHolder.subList(closeHolder.size() - configHolder.getSMALength(), closeHolder.size());
    }
}
