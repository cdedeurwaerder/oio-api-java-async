package io.openio.sds.common.logging;

public abstract class SDSLoggerFactory {

    private static volatile SDSLoggerFactory defaultFactory = new Log4j2SDSLoggerFactory();

    public static SDSLogger getLogger(String prefix, String name) {
        return defaultFactory.newInstance(prefix == null ? null: prefix.intern(), name.intern());
    }

    public static SDSLogger getLogger(String name) {
        return defaultFactory.newInstance(name.intern());
    }

    public SDSLogger newInstance(String name) {
        return newInstance(null, name);
    }

    protected abstract SDSLogger newInstance(String prefix, String name);

}
