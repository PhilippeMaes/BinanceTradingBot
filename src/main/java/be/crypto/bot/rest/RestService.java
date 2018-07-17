package be.crypto.bot.rest;

import be.crypto.bot.data.ClosedTradeService;
import be.crypto.bot.domain.DTO.OpenPositionDTO;
import be.crypto.bot.service.exchange.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("/closedTrades")
    public ResponseEntity<?> getClosedTrades() {
        return ResponseEntity.status(HttpStatus.OK).body(Factory.fromTrades(closedTradeService.getTrades()));
    }

    @RequestMapping("/openTrades")
    public ResponseEntity<?> getOpenTrades() {
        List<OpenPositionDTO> openPositionDTOs = closedTradeService.getOpenPositions().stream().map(t -> Factory.createOpenPositionDTO(t, Double.valueOf(webService.getTicker(t.getMarketName()).getLastPrice()))).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(openPositionDTOs);
    }
}
