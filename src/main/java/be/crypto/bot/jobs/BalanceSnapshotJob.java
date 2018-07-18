package be.crypto.bot.jobs;

import be.crypto.bot.data.BalanceSnapshotRepository;
import be.crypto.bot.data.holders.BalanceHolder;
import be.crypto.bot.domain.BalanceSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Created by philippemaes on 18/07/2018.
 */
@Service
public class BalanceSnapshotJob {

    @Autowired
    private BalanceHolder balanceHolder;

    @Autowired
    private BalanceSnapshotRepository repository;

    @Scheduled(cron = "0 0 0 * * ?")
    private void takeSnapshot() {
        repository.save(new BalanceSnapshot(balanceHolder.getTotalBaseBalance()));
    }
}
