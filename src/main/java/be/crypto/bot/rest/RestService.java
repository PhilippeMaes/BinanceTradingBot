package be.crypto.bot.rest;

import be.crypto.bot.data.BalanceSnapshotRepository;
import be.crypto.bot.data.ClosedTradeService;
import be.crypto.bot.data.ConfigHolder;
import be.crypto.bot.data.holders.BalanceHolder;
import be.crypto.bot.domain.BalanceSnapshot;
import be.crypto.bot.domain.DTO.ConfigDTO;
import be.crypto.bot.domain.DTO.OpenPositionDTO;
import be.crypto.bot.service.exchange.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by philippemaes on 18/06/2018.
 */
@RestController
public class RestService {

    @Autowired
    private ClosedTradeService closedTradeService;

    @Autowired
    private WebService webService;

    @Autowired
    private BalanceHolder balanceHolder;

    @Autowired
    private ConfigHolder configHolder;

    @Autowired
    private BalanceSnapshotRepository balanceSnapshotRepository;

    @RequestMapping("/chartData")
    public ResponseEntity<?> getChartData() {
        List<BalanceSnapshot> balanceSnapshots = balanceSnapshotRepository.findAllByOrderByTimestampDesc();
        return ResponseEntity.status(HttpStatus.OK).body(Factory.fromBalances(balanceSnapshots));
    }

    @RequestMapping("/closedTrades")
    public ResponseEntity<?> getClosedTrades() {
        return ResponseEntity.status(HttpStatus.OK).body(Factory.fromTrades(closedTradeService.getTrades()));
    }

    @RequestMapping("/openTrades")
    public ResponseEntity<?> getOpenTrades() {
        List<OpenPositionDTO> openPositionDTOs = closedTradeService.getOpenPositions().stream().map(t -> Factory.createOpenPositionDTO(t, Double.valueOf(webService.getTicker(t.getMarketName()).getLastPrice()))).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(openPositionDTOs);
    }

    @RequestMapping("/balance")
    public ResponseEntity<?> getBalance() {
        return ResponseEntity.status(HttpStatus.OK).body(balanceHolder.getTotalBaseBalance());
    }

    @RequestMapping("/balanceChange")
    public ResponseEntity<?> getBalanceChange() {
        Double change = balanceHolder.getTotalBaseBalance() / balanceSnapshotRepository.findFirstByOrderByTimestampDesc().getBalance();
        change = change.equals(Double.NaN) ? 0.0 : (change - 1.0) * 100.0;
        return ResponseEntity.status(HttpStatus.OK).body(change);
    }

    @RequestMapping(value = "/config", method = RequestMethod.POST)
    public ResponseEntity<?> updateConfig(@RequestBody ConfigDTO configDTO) {
        if (configDTO == null) {
            throw new IllegalArgumentException("Body can't be null");
        }
        if (!configDTO.isValid()) {
            throw new IllegalArgumentException("ConfigDTO is invalid");
        }

        configHolder.setSMALength(configDTO.getSMALength());
        configHolder.setBuyPercentageTrigger(configDTO.getBuyPercentageTrigger());
        configHolder.setSellPercentageTrigger(configDTO.getSellPercentageTrigger());
        configHolder.setMaxOrderSize(configDTO.getMaxOrderSize());

        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }
}
