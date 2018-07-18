package be.crypto.bot.domain.DTO;

import java.util.List;

/**
 * Created by philippemaes on 18/07/2018.
 */
public class BalanceDTO {

    private List<String> dates;
    private List<Double> values;

    public BalanceDTO(List<String> dates, List<Double> values) {
        this.dates = dates;
        this.values = values;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }
}