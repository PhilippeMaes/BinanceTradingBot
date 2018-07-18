package be.crypto.bot.data;

import be.crypto.bot.domain.BalanceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by philippemaes on 07/02/2018.
 */
@Repository
public interface BalanceSnapshotRepository extends JpaRepository<BalanceSnapshot, Long> {

    BalanceSnapshot findFirstByOrderByTimestampDesc();

    List<BalanceSnapshot> findAllByOrderByTimestampDesc();
}
