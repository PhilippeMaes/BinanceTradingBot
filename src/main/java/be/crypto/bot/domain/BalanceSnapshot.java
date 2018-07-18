package be.crypto.bot.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by philippemaes on 18/07/2018.
 */
@Entity
@Table(name = "BALANCE_SNAPSHOT")
public class BalanceSnapshot {

    @Id
    @GeneratedValue
    private Long id;

    private Double balance;
    private String formattedDate;
    private Long timestamp;

    private BalanceSnapshot() {
    }

    public BalanceSnapshot(Double balance) {
        this.balance = balance;
        this.timestamp = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(this.timestamp));
        cal.add(Calendar.DATE, -1);
        this.formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public Double getBalance() {
        return balance;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
