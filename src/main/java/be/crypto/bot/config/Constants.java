package be.crypto.bot.config;

/**
 * Created by philippemaes on 15/06/2018.
 */
public class Constants {

    public static final Double TRANSACTION_FEE = 0.0005;
    public static final String BINANCE_API_KEY = "R88eYFZ0fj7AnBKvyNDOwM3hoinUwUeRDaZ1kfbO1gIzRP9mwveUDNqd0981FM7L";
    public static final String BINANCE_API_SECRET = "oZToInMoge8seNw4UMcZkGEC2EcWIYYepx9AWbwck3lGmOMIjXei9X9PEwVcNUsh";
    public static final String BINANCE_UNKNOWN_ORDER_ERROR = "UKNOWN_ORDER";

    public static final String PUSHOVER_API_TOKEN = "adrfw3nkuib313r5y1n787c6pogcbw";
    public static final String PUSHOVER_USER_KEY = "ucuft9qn8muzu4z1zayx5znovqtcrn";
    public static final String PUSHOVER_DEVICE_NAME = "GS8";

    public static final Integer EMA_PERIOD = 100;
    public static final Double BUY_PERC_TRIGGER = 0.05;
    public static final Double AVERAGING_DOWN_PERC_TRIGGER = 0.05;
    public static final Double SELL_PERC_TRIGGER = 0.0;
    public static final Integer RSI_PERIOD = 14;
    public static final Integer BOLLINGER_PERIOD = 20;

    public static final String BASE = "BTC";
    public static final Double MAX_INITIAL_BASE = 0.080;
    public static final Double MAX_ORDER_SIZE = 0.020;
    public static final Double AVERAGING_DOWN_BALANCE = 0.020;

    public static final String[] BLACK_LIST = { "TUSD" };
}
