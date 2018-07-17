package be.crypto.bot;

import be.crypto.bot.config.Constants;
import be.crypto.bot.domain.MarketState;
import be.crypto.bot.domain.RSIResult;
import be.crypto.bot.service.CalculationService;
import be.crypto.bot.service.exchange.WebService;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MovingAverageBotApplicationTests {

	private static final Logger log = LoggerFactory.getLogger(MovingAverageBotApplicationTests.class);
	private static final Double BUY_PERC_TRIGGER = 0.05;
	private static final Double AVERAGE_DOWN_PERC_TRIGGER = 0.10;
	private static final Double SELL_PERC_TRIGGER = 0.025;
	private static final Integer EMA_PERIOD = 100;
	private static final Integer LONG_EMA_PERIOD = 200;

	private RSIResult WildersRSI = null;
	private List<Double> RSIHolder;
	private List<Double> closeHolder;
	private List<Double> MACD_EMADiffHolder;

	private Double previousClose = null;
	private Double EMA = null;
	private Double StochRSI = null;
	private Double SMA = null;
	private Double LongSMA = null;
	private Double EMA12 = null;
	private Double EMA26 = null;
	private Double EMA9 = null;
	private Double MACD = null;

	private Integer trades = 0;
	private Integer badTrades = 0;

	private List<Pair<Long, Long>> tradeList = new ArrayList<>();

	@Autowired
	private WebService webService;

	@Autowired
	private CalculationService calculationService;

	@Test
	public void contextLoads() throws ParseException {
		List<TickerPrice> tickers = webService.getTickers();
		List<String> markets = tickers.stream().filter(t -> t.getSymbol().endsWith(Constants.BASE)).map(t -> t.getSymbol().substring(0, t.getSymbol().length() - 3)).collect(Collectors.toList());

		double totalReturn = 0.0;
		double totalMarketGain = 0.0;

		for (String market : markets) {
			if (Arrays.asList(Constants.BLACK_LIST).contains(market))
				continue;

			TestResult testResult = doTest(market);
			totalReturn += testResult.getProfit();
			totalMarketGain += testResult.getMarketGain();
		}

		int tradesAtOnce = 0;
		for (Pair<Long, Long> trade : tradeList) {
			int trades = 1;
			for (Pair<Long, Long> otherTrade : tradeList) {
				if (otherTrade.getFirst() > trade.getFirst() && otherTrade.getFirst() < trade.getSecond())
					trades++;
			}
			if (trades > tradesAtOnce)
				tradesAtOnce = trades;
		}

		log.info("TOTAL -> Profit/Loss: " + String.format("%.8f", totalReturn) + " | Wins: " + (1.0 - (double) badTrades/trades) + " | Market change: " + (totalMarketGain / markets.size()) + " | Trades at once: " + tradesAtOnce);
	}

	private TestResult doTest(String market) throws ParseException {
		initiate();

		Double base = 1.0;
		Double quantityAvailable = 0.0;
		Double close = null;
		Integer candlesSinceBuy = null;
		Double firstClose = null;
		Double previousBaseValue = null;
		Double lastBuyPrice = null;
		Long startTimeStamp = null;
		boolean averagedDown = false;

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss Z");
		Date startDate = df.parse("26/06/2018 16:44:00 +0200");

		Double volume = 0.0;

		List<Candlestick> candleSticks = webService.getCandleSticks(Constants.BASE, market, CandlestickInterval.FIVE_MINUTES);
		for (Candlestick candlestick : candleSticks) {
			close = Double.valueOf(candlestick.getClose());
			Double low = Double.valueOf(candlestick.getLow());
			Double high = Double.valueOf(candlestick.getHigh());
			volume += Double.valueOf(candlestick.getQuoteAssetVolume());
			Double buyVolume = Double.valueOf(candlestick.getTakerBuyQuoteAssetVolume());
			Optional<MarketState> marketState = getMarketState(close);
			String dateTime = df.format(new Date(candlestick.getCloseTime()));

			if (!marketState.isPresent() || WildersRSI == null || StochRSI == null || MACD == null || LongSMA == null)
				continue;
			if (candlesSinceBuy != null)
				candlesSinceBuy++;

			Date candleDate = new Date(candlestick.getCloseTime());
			if (candleDate.before(startDate))
				continue;
			if (firstClose == null)
				firstClose = close;

			if (quantityAvailable > 0.0) {
				if (high >= SMA * (1 + SELL_PERC_TRIGGER)) {
					// sell
					Double price;
					if (high >= SMA * (1 + SELL_PERC_TRIGGER)) {
						price = SMA * (1 + SELL_PERC_TRIGGER);
					} else {
						price = close;
					}

					log.info("[" + market + "] SELL @ price " + String.format("%.8f", price) + " -- " + dateTime);
					base += quantityAvailable * price * (1 - Constants.TRANSACTION_FEE);
					quantityAvailable = 0.0;
					candlesSinceBuy = null;
					if (base < previousBaseValue)
						badTrades++;
					trades++;
					tradeList.add(Pair.of(startTimeStamp, candlestick.getCloseTime()));
					averagedDown = false;
				}
				if (!averagedDown && low <= SMA * (1.0 - AVERAGE_DOWN_PERC_TRIGGER)) {
					log.info("[" + market + "] AVERAGE DOWN @ price " + String.format("%.8f", SMA * (1 - AVERAGE_DOWN_PERC_TRIGGER)) + " -- " + dateTime);
					Double candleVolume = 0.0125;
//					Double candleVolume = Double.valueOf(candlestick.getQuoteAssetVolume()) / 50;
					quantityAvailable += candleVolume / (SMA * (1 - AVERAGE_DOWN_PERC_TRIGGER)) * (1 - Constants.TRANSACTION_FEE);
					previousBaseValue = base;
					lastBuyPrice = SMA * (1 - AVERAGE_DOWN_PERC_TRIGGER);
					base -= candleVolume;
					candlesSinceBuy = 1;
					startTimeStamp = candlestick.getCloseTime();
					averagedDown = true;
				}
			} else {
				if (low <= SMA * (1.0 - BUY_PERC_TRIGGER)) {
//					if (candleDate.after(endDate))
//						continue;

					// buy
					log.info("[" + market + "] BUY @ price " + String.format("%.8f", SMA * (1 - BUY_PERC_TRIGGER)) + " -- " + dateTime);
					Double candleVolume = 0.025;
//					Double candleVolume = Double.valueOf(candlestick.getQuoteAssetVolume()) / 50;
					quantityAvailable = candleVolume / (SMA * (1 - BUY_PERC_TRIGGER)) * (1 - Constants.TRANSACTION_FEE);
					previousBaseValue = base;
					lastBuyPrice = SMA * (1 - BUY_PERC_TRIGGER);
					base -= candleVolume;
					candlesSinceBuy = 1;
					startTimeStamp = candlestick.getCloseTime();
				}
			}
		}

		if (quantityAvailable > 0.0) {
			log.info("[" + market + "] SELL @ price " + String.format("%.8f", close));
			base += quantityAvailable * close * (1 - Constants.TRANSACTION_FEE);
		}

		if (firstClose == null)
			firstClose = Double.valueOf(candleSticks.get(0).getClose());

//		log.info("[" + market + "] Profit/loss: " + String.format("%.8f", base - 1.0) + " | Volume average: " + ((double) volume / candleSticks.size()));
		return new TestResult(close / firstClose, base - 1.0);
	}

	private void initiate() {
		this.closeHolder = new ArrayList<>();
		this.RSIHolder = new ArrayList<>();
		this.MACD_EMADiffHolder = new ArrayList<>();
		this.SMA = null;
		this.LongSMA = null;
		this.EMA = null;
		this.EMA12 = null;
		this.EMA26 = null;
		this.EMA9 = null;
		this.StochRSI = null;
		this.WildersRSI = null;
	}

	public Optional<MarketState> getMarketState(Double close) {
		if (closeHolder.size() >= Math.max(26, EMA_PERIOD)) {
			if (closeHolder.size() >= EMA_PERIOD) {
				double previousEMA = EMA != null ? EMA : closeHolder.subList(closeHolder.size() - EMA_PERIOD, closeHolder.size()).stream().mapToDouble(Double::doubleValue).sum() / EMA_PERIOD;
				EMA = calculationService.getEMA(EMA_PERIOD, previousEMA, close);
			}

			if (closeHolder.size() >= Constants.RSI_PERIOD) {
				if (WildersRSI == null) {
					WildersRSI = calculationService.getInitialRSI(closeHolder.subList(closeHolder.size() - Constants.RSI_PERIOD, closeHolder.size()), close);
				} else {
					double gain = close - previousClose;
					WildersRSI = calculationService.getWildersRSI(WildersRSI, gain);
				}
				RSIHolder.add(WildersRSI.getRSI());
				if (RSIHolder.size() >= Constants.RSI_PERIOD) {
					StochRSI = calculationService.getStochRSI(RSIHolder.subList(RSIHolder.size() - Constants.RSI_PERIOD, RSIHolder.size()));
				}
			}

			if (closeHolder.size() >= 12) {
				double previousAverage = EMA12 != null ? EMA12 : closeHolder.subList(closeHolder.size() - 12, closeHolder.size()).stream().mapToDouble(Double::doubleValue).sum() / 12;
				EMA12 = calculationService.getEMA(12, previousAverage, close);
			}
			if (closeHolder.size() >= 26) {
				double previousAverage = EMA26 != null ? EMA26 : closeHolder.subList(closeHolder.size() - 26, closeHolder.size()).stream().mapToDouble(Double::doubleValue).sum() / 26;
				EMA26 = calculationService.getEMA(26, previousAverage, close);
			}
			if (MACD_EMADiffHolder.size() >= 8) {
				double previousAverage = EMA9 != null ? EMA9 : MACD_EMADiffHolder.subList(MACD_EMADiffHolder.size() - 8, MACD_EMADiffHolder.size()).stream().mapToDouble(Double::doubleValue).sum() / 8;
				EMA9 = calculationService.getEMA(8, previousAverage, EMA12 - EMA26);
				MACD = EMA12 - EMA26 - EMA9;
			}

			// add values to holders
			if (EMA12 != null && EMA26 != null) {
				MACD_EMADiffHolder.add(EMA12 - EMA26);
			}
		}

		closeHolder.add(close);
		previousClose = close;

		if (closeHolder.size() >= EMA_PERIOD) {
			SMA = closeHolder.subList(closeHolder.size() - EMA_PERIOD, closeHolder.size()).stream().mapToDouble(Double::doubleValue).sum() / EMA_PERIOD;
		}
		if (closeHolder.size() >= LONG_EMA_PERIOD) {
			LongSMA = closeHolder.subList(closeHolder.size() - LONG_EMA_PERIOD, closeHolder.size()).stream().mapToDouble(Double::doubleValue).sum() / LONG_EMA_PERIOD;
		}

		return EMA != null && WildersRSI != null && StochRSI != null ? Optional.of(new MarketState(SMA, EMA, WildersRSI, StochRSI)) : Optional.empty();
	}
}

class TestResult {
	private Double marketGain;
	private Double profit;

	public TestResult(Double marketGain, Double profit) {
		this.marketGain = marketGain;
		this.profit = profit;
	}

	public Double getMarketGain() {
		return marketGain;
	}

	public Double getProfit() {
		return profit;
	}
}
