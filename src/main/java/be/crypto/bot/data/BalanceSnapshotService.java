package be.crypto.bot.data;

import be.crypto.bot.domain.BalanceSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by philippemaes on 08/02/2018.
 */
@Service
public class BalanceSnapshotService {

    @Autowired
    private BalanceSnapshotRepository repository;

    public void addBalanceSnapshot(BalanceSnapshot balanceSnapshot) {
        repository.save(balanceSnapshot);
    }

    public List<BalanceSnapshot> getBalanceSnapshots() {
        return repository.findAll();
    }

    public List<BalanceSnapshot> getBalanceSnapshotsDesc() {
        return repository.findAllByOrderByTimestampDesc();
    }

    public Double getLatestBalanceSnapshot() {
        BalanceSnapshot snapshot = repository.findFirstByOrderByTimestampDesc();
        return snapshot != null ? snapshot.getBalance() : 0.0;
    }
}
