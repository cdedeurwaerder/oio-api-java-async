package io.openio.sds.common.logging;

public class Loggers {

    private final static String commonPrefix = System.getProperty("sds.logger.prefix", "io.openio.sds.");

    public static SDSLogger getLogger(String name) {
        return SDSLoggerFactory.getLogger(getLoggerName(name));
    }

    public static SDSLogger getLogger(Class<?> clazz) {
        return SDSLoggerFactory.getLogger(getLoggerName(buildClassLoggerName(clazz)));
    }

    private static String getLoggerName(String name) {
        return commonPrefix + name;
    }

    private static String buildClassLoggerName(Class<?> clazz) {
        return clazz.getName();
    }
}
