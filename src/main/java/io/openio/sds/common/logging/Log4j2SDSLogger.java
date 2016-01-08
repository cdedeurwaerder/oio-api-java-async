package io.openio.sds.common.logging;


import org.apache.logging.log4j.Logger;

public class Log4j2SDSLogger extends SDSLogger {

    private final Logger logger;

    public Log4j2SDSLogger(String prefix, Logger logger) {
        super(prefix);
        this.logger = logger;
    }

    @Override
    protected void internalInfo(String msg, Object... params) {
        logger.info(msg, params);
    }

    @Override
    protected void internalInfo(String msg, Throwable t, Object... params) {
        logger.info(msg, t, params);
    }

    @Override
    protected void internalDebug(String msg, Object... params) {
        logger.debug(msg, params);
    }

    @Override
    protected void internalDebug(String msg, Throwable t, Object... params) {
        logger.debug(msg, t, params);
    }

    @Override
    protected void internalWarn(String msg, Object... params) {
        logger.warn(msg, params);
    }

    @Override
    protected void internalWarn(String msg, Throwable t, Object... params) {
        logger.warn(msg, t, params);
    }

    @Override
    protected void internalTrace(String msg, Object... params) {
        logger.trace(msg, params);
    }

    @Override
    protected void internalTrace(String msg, Throwable t, Object... params) {
        logger.trace(msg, t, params);
    }

    @Override
    protected void internalError(String msg, Object... params) {
        logger.error(msg, params);
    }

    @Override
    protected void internalError(String msg, Throwable t, Object... params) {
        logger.error(msg, t, params);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }
}
