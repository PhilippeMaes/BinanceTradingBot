package be.crypto.bot.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private Long timestamp;

    private BalanceSnapshot() {
    }

    public BalanceSnapshot(Double balance) {
        this.balance = balance;
        this.timestamp = System.currentTimeMillis();
    }

    public Double getBalance() {
        return balance;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}
