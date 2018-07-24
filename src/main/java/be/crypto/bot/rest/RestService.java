package be.crypto.bot.rest;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.BalanceSnapshotRepository;
import be.crypto.bot.data.ClosedTradeService;
import be.crypto.bot.data.ConfigHolder;
import be.crypto.bot.data.holders.BalanceHolder;
import be.crypto.bot.domain.BalanceSnapshot;
import be.crypto.bot.domain.DTO.ConfigDTO;
import be.crypto.bot.domain.DTO.OpenPositionDTO;
import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.general.SymbolInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        List<OpenPositionDTO> openPositionDTOs = closedTradeService.getOpenPositions().stream()
                .filter(c -> c.getQuantity() > Double.valueOf(webService.getSymbolInfo(Constants.BASE, c.getMarketName()).getFilters().get(1).getMinQty()))
                .map(t -> Factory.createOpenPositionDTO(t, Double.valueOf(webService.getTicker(t.getMarketName()).getLastPrice()))).collect(Collectors.toList());
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

    @RequestMapping(value = "/marketSell", method = RequestMethod.POST)
    public ResponseEntity<?> marketSell(@RequestParam("market") String market) {
        if (market == null) {
            throw new IllegalArgumentException("Market can't be null");
        }

        // get quantity
        Double quantity = balanceHolder.getBalance(market);

        // get symbol info
        SymbolInfo symbolInfo = webService.getSymbolInfo(Constants.BASE, market);
        if (quantity < Double.valueOf(symbolInfo.getFilters().get(1).getMinQty())) {
            throw new IllegalArgumentException("Quantity too low");
        }

        webService.placeMarketSellOrder(Constants.BASE, market, quantity);

        return ResponseEntity.status(HttpStatus.OK).body("Success");
    }
}
