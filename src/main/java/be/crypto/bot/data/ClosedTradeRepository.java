package be.crypto.bot.data;

import be.crypto.bot.domain.ClosedTrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by philippemaes on 07/02/2018.
 */
@Repository
public interface ClosedTradeRepository extends JpaRepository<ClosedTrade, Long> {

    List<ClosedTrade> findByMarketName(String marketName);

    List<ClosedTrade> findByMarketNameOrderByTimestampDesc(String marketName);

    ClosedTrade findFirstByMarketNameOrderByTimestampDesc(String marketName);
}
