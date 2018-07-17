package be.crypto.bot.service.exchange;

import be.crypto.bot.config.Constants;
import be.crypto.bot.domain.OpenOrder;
import be.crypto.bot.domain.OrderType;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.*;
import com.binance.api.client.domain.account.request.CancelOrderRequest;
import com.binance.api.client.domain.account.request.OrderStatusRequest;
import com.binance.api.client.domain.general.SymbolInfo;
import com.binance.api.client.domain.market.*;
import com.binance.api.client.exception.BinanceApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

import static com.binance.api.client.domain.account.NewOrder.*;

/**
 * Created by philippemaes on 15/06/2018.
 */
@Service
public class WebService {

    private static final Logger log = LoggerFactory.getLogger(WebService.class);

    private BinanceApiRestClient client;
    private Map<String, SymbolInfo> symbolInfo;

    @PostConstruct
    private void init() {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(Constants.BINANCE_API_KEY, Constants.BINANCE_API_SECRET);
        client = factory.newRestClient();

        this.symbolInfo = new HashMap<>();

        client.getExchangeInfo().getSymbols()
                .stream()
                .forEach(s -> symbolInfo.put(s.getSymbol(), s));
    }

    public Optional<OrderBook> getOrderBook(String base, String marketName, int maxTries) {
        int count = 0;
        while (true) {
            try {
                return Optional.of(getOrderBook(base, marketName));
            } catch (IOException e) {
                if (++count == maxTries) {
                    log.error("Failed to get OrderBook for " + marketName + " after " + maxTries + " tries", e);
                    return Optional.empty();
                }
            }
        }
    }

    public OrderBook getOrderBook(String base, String marketName) throws IOException {
        return client.getOrderBook(marketName + base, 100);
    }

    public TickerStatistics getTicker(String marketName) {
        return client.get24HrPriceStatistics(marketName + Constants.BASE);
    }

    public List<TickerPrice> getTickers() {
        return client.getAllPrices();
    }

    public OpenOrder placeLimitBuyOrder(String base, String marketName, double quantity, double rate, TimeInForce tif) {
        SymbolInfo symbolInfo = this.symbolInfo.get(marketName + base);
        NewOrder newOrder = limitBuy(
                marketName + base,
                tif,
                getFormattedAmount(Double.valueOf(symbolInfo.getFilters().get(1).getStepSize()), quantity),
                getFormattedAmount(Double.valueOf(symbolInfo.getFilters().get(0).getTickSize()), rate));
        NewOrderResponse response = client.newOrder(newOrder);
        return new OpenOrder(marketName, response.getOrderId(), Double.valueOf(newOrder.getQuantity()), Double.valueOf(newOrder.getPrice()), OrderType.BUY);
    }

    public OpenOrder placeLimitSellOrder(String base, String marketName, double quantity, double rate) {
        SymbolInfo symbolInfo = this.symbolInfo.get(marketName + base);
        NewOrder newOrder = limitSell(
                marketName + base,
                TimeInForce.GTC,
                getFormattedAmount(Double.valueOf(symbolInfo.getFilters().get(1).getStepSize()), quantity),
                getFormattedAmount(Double.valueOf(symbolInfo.getFilters().get(0).getTickSize()), rate));
        NewOrderResponse response = client.newOrder(newOrder);
        return new OpenOrder(marketName, response.getOrderId(), Double.valueOf(newOrder.getQuantity()), Double.valueOf(newOrder.getPrice()), OrderType.SELL);
    }

    public Long placeMarketBuyOrder(String base, String marketName, double quantity) {
        SymbolInfo symbolInfo = this.symbolInfo.get(marketName + base);
        NewOrderResponse newOrderResponse = client.newOrder(marketBuy(marketName + base, getFormattedAmount(Double.valueOf(symbolInfo.getFilters().get(1).getStepSize()), quantity)));
        return newOrderResponse.getOrderId();
    }

    public Long placeMarketSellOrder(String base, String marketName, double quantity) {
        SymbolInfo symbolInfo = this.symbolInfo.get(marketName + base);
        NewOrderResponse newOrderResponse = client.newOrder(marketSell(marketName + base, getFormattedAmount(Double.valueOf(symbolInfo.getFilters().get(1).getStepSize()), quantity)));
        return newOrderResponse.getOrderId();
    }

    public Order getOrder(String base, String marketName, long orderId) throws BinanceApiException {
        return client.getOrderStatus(new OrderStatusRequest(marketName + base, orderId));
    }

    public void cancelOrder(String base, String marketName, long orderId) throws BinanceApiException {
        client.cancelOrder(new CancelOrderRequest(marketName + base, orderId));
    }

    public List<Trade> getTrades(String base, String marketName) {
        return client.getMyTrades(marketName + base);
    }

    public AssetBalance getBalance(String marketName) {
        return client.getAccount().getAssetBalance(marketName);
    }

    public List<AssetBalance> getBalances() {
        return client.getAccount().getBalances();
    }

    public SymbolInfo getSymbolInfo(String base, String marketName) {
        return this.symbolInfo.get(marketName + base);
    }

    public static String getFormattedAmount(Double precision, Double value) {
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(8); // 8 = Max BTC precision
        String[] results = df.format(precision).split("\\.");
        int decimals = results.length == 1 ? 0 : results[1].length();

        return BigDecimal.valueOf(value)
                .setScale(decimals, RoundingMode.DOWN)
                .toString();
    }

    public List<Candlestick> getCandleSticks(String base, String marketName, CandlestickInterval interval) {
        return client.getCandlestickBars(marketName + base, interval);
    }
}
