package be.crypto.bot.data;

import be.crypto.bot.config.Constants;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Created by philippemaes on 17/07/2018.
 */
@Service
public class ConfigHolder {

    private Double buyPercentageTrigger;
    private Double sellPercentageTrigger;
    private Double maxOrderSize;
    private Integer SMALength;

    @PostConstruct
    private void init() {
        this.buyPercentageTrigger = Constants.BUY_PERC_TRIGGER;
        this.sellPercentageTrigger = Constants.SELL_PERC_TRIGGER;
        this.maxOrderSize = Constants.MAX_INITIAL_BASE;
        this.SMALength = Constants.EMA_PERIOD;
    }

    public Double getBuyPercentageTrigger() {
        return buyPercentageTrigger;
    }

    public void setBuyPercentageTrigger(Double buyPercentageTrigger) {
        this.buyPercentageTrigger = buyPercentageTrigger;
    }

    public Double getSellPercentageTrigger() {
        return sellPercentageTrigger;
    }

    public void setSellPercentageTrigger(Double sellPercentageTrigger) {
        this.sellPercentageTrigger = sellPercentageTrigger;
    }

    public Double getMaxOrderSize() {
        return maxOrderSize;
    }

    public void setMaxOrderSize(Double maxOrderSize) {
        this.maxOrderSize = maxOrderSize;
    }

    public Integer getSMALength() {
        return SMALength;
    }

    public void setSMALength(Integer SMALength) {
        this.SMALength = SMALength;
    }
}
