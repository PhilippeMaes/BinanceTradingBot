package be.crypto.bot.domain;

/**
 * Created by philippemaes on 03/10/2018.
 */
public class OpenPosition {

    private String marketName;
    private Double entry;
    private Boolean averagedDown;

    public OpenPosition(String marketName, Double entry) {
        this.marketName = marketName;
        this.entry = entry;
        this.averagedDown = false;
    }

    public String getMarketName() {
        return marketName;
    }

    public Double getEntry() {
        return entry;
    }

    public Boolean isAveragedDown() {
        return averagedDown;
    }

    public void setAveragedDown(Boolean averagedDown) {
        this.averagedDown = averagedDown;
    }
}
