package be.crypto.bot.domain.DTO;

/**
 * Created by philippemaes on 18/07/2018.
 */
public class ConfigDTO {

    private Double buyPercentageTrigger;
    private Double sellPercentageTrigger;
    private Double maxOrderSize;
    private Integer SMALength;

    public ConfigDTO(Double buyPercentageTrigger, Double sellPercentageTrigger, Double maxOrderSize, Integer SMALength) {
        this.buyPercentageTrigger = buyPercentageTrigger;
        this.sellPercentageTrigger = sellPercentageTrigger;
        this.maxOrderSize = maxOrderSize;
        this.SMALength = SMALength;
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

    public boolean isValid() {
        if (SMALength == null) return false;
        if (buyPercentageTrigger == null) return false;
        if (sellPercentageTrigger == null) return false;
        if (maxOrderSize == null) return false;
        return true;
    }
}
