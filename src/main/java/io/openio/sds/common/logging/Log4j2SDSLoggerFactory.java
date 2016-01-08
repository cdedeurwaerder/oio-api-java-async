package io.openio.sds.common.logging;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4j2SDSLoggerFactory extends SDSLoggerFactory {
    @Override
    protected SDSLogger newInstance(String prefix, String name) {
        final Logger logger = LogManager.getLogger(name);
        return new Log4j2SDSLogger(prefix, logger);
    }
}
