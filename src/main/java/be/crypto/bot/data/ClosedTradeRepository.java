package be.crypto.bot.data;

import be.crypto.bot.domain.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by philippemaes on 07/02/2018.
 */
@Repository
public interface ClosedTradeRepository extends JpaRepository<Trade, Long> {

    List<Trade> findByMarketName(String marketName);

    List<Trade> findByMarketNameOrderByTimestampDesc(String marketName);

    Trade findFirstByMarketNameOrderByTimestampDesc(String marketName);
}
