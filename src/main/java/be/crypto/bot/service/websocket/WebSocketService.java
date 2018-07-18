package be.crypto.bot.service.websocket;

import be.crypto.bot.config.Constants;
import be.crypto.bot.data.holders.MarketStateManager;
import be.crypto.bot.domain.MarketTicker;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neovisionaries.ws.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Created by philippemaes on 16/06/2018.
 */
@Service
public class WebSocketService {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private static final String BINANCE_SOCKET_URL = "wss://stream.binance.com:9443/ws/";

    private ObjectMapper mapper;

    @Autowired
    private MarketStateManager marketStateManager;

    @PostConstruct
    private void init() throws IOException {
        this.mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        subScribeToMarketTickers();
    }

    private void subScribeToMarketTickers() throws IOException {
        // setup socket
        String path = "!ticker@arr";
        final WebSocket ws = new WebSocketFactory().createSocket(BINANCE_SOCKET_URL + path);

        // create listener
        ws.addListener(new WebSocketAdapter() {

            @Override
            public void onTextMessage(WebSocket websocket, String data) throws Exception {
                MarketTicker[] marketTickers = mapper.readValue(data, MarketTicker[].class);
                for (MarketTicker marketTicker : marketTickers)
                    if (marketTicker.getSymbol().endsWith(Constants.BASE)) updateTicker(marketTicker);
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                log.info("Market tickers WebSocket disconnected, reconnecting");

                // reconnect -> make new websocket (connect() on the old one does not seem to work
                subScribeToMarketTickers();
            }
        });

        // connect
        connectWS(ws);
    }

    private boolean connectWS(WebSocket ws) {
        int maxTries = 5;
        int count = 0;
        while (true) {
            try {
                ws.connect();
                return true;
            } catch (WebSocketException e) {
                if (++count == maxTries) {
                    log.error("Failed to connect to WebSocket after " + maxTries + " tries");
                    return false;
                }
            }
        }
    }

    private void updateTicker(MarketTicker marketTicker) {
        marketTicker.setSymbol(marketTicker.getSymbol().substring(0, marketTicker.getSymbol().length() - 3));
        marketStateManager.updateTicker(marketTicker.getSymbol(), marketTicker);
    }
}
