package be.crypto.bot.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by philippemaes on 16/10/2018.
 */
public class CustomLogger {

    private String lastLogged = "";
    private Logger logger;

    public CustomLogger(Class myClass) {
        this.logger = LoggerFactory.getLogger(myClass);
    }

    public void debug(String s) {
        if (!lastLogged.equals(s))
            logger.debug(s);
        lastLogged = s;
    }

    public void info(String s) {
        if (!lastLogged.equals(s))
            logger.info(s);
        lastLogged = s;
    }

    public void warn(String s) {
        if (!lastLogged.equals(s))
            logger.warn(s);
        lastLogged = s;
    }

    public void error(String s) {
        if (!lastLogged.equals(s))
            logger.error(s);
        lastLogged = s;
    }

    public void error(String s, Exception ex) {
        if (!lastLogged.equals(s))
            logger.error(s, ex);
        lastLogged = s;
    }
}
