package be.crypto.bot.domain.DTO;

/**
 * Created by philippemaes on 18/06/2018.
 */
public class OpenPositionDTO {

    private String marketName;
    private String averageEntry;
    private String currentPrice;
    private String quantity;
    private String PL;
    private String date;

    public OpenPositionDTO(String marketName, String averageEntry, String currentPrice, String quantity, String PL, String date) {
        this.marketName = marketName;
        this.averageEntry = averageEntry;
        this.currentPrice = currentPrice;
        this.quantity = quantity;
        this.PL = PL;
        this.date = date;
    }

    public String getMarketName() {
        return marketName;
    }

    public void setMarketName(String marketName) {
        this.marketName = marketName;
    }

    public String getAverageEntry() {
        return averageEntry;
    }

    public void setAverageEntry(String averageEntry) {
        this.averageEntry = averageEntry;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
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
