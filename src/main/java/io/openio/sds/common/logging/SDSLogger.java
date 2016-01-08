package io.openio.sds.common.logging;

public abstract class SDSLogger {

    private String prefix;

    protected SDSLogger(String prefix) {
        this.prefix = prefix;
    }

    protected String getPrefix() {
        return prefix;
    }

    public void info(String msg, Object... params) {
        internalInfo(msg, params);
    }

    public void info(String msg, Throwable t, Object... params) {
        internalInfo(msg, t, params);
    }

    public void trace(String msg, Object... params) {
        internalTrace(msg, params);
    }

    public void trace(String msg, Throwable t, Object... params) {
        internalTrace(msg, t, params);
    }

    public void debug(String msg, Object... params) {
        internalDebug(msg, params);
    }

    public void debug(String msg, Throwable t, Object... params) {
        internalDebug(msg, t, params);
    }

    public void warn(String msg, Object... params) {
        internalWarn(msg, params);
    }

    public void warn(String msg, Throwable t, Object... params) {
        internalWarn(msg, t, params);
    }

    public void error(String msg, Object... params) {
        internalError(msg, params);
    }

    public void error(String msg, Throwable t, Object... params) {
        internalError(msg, t, params);
    }
    
    public abstract boolean isInfoEnabled();

    public abstract boolean isDebugEnabled();

    public abstract boolean isWarnEnabled();

    public abstract boolean isErrorEnabled();

    public abstract boolean isTraceEnabled();

    protected abstract void internalInfo(String msg, Object... params);

    protected abstract void internalInfo(String msg, Throwable t, Object... params);

    protected abstract void internalDebug(String msg, Object... params);

    protected abstract void internalDebug(String msg, Throwable t, Object... params);

    protected abstract void internalWarn(String msg, Object... params);

    protected abstract void internalWarn(String msg, Throwable t, Object... params);

    protected abstract void internalTrace(String msg, Object... params);

    protected abstract void internalTrace(String msg, Throwable t, Object... params);

    protected abstract void internalError(String msg, Object... params);

    protected abstract void internalError(String msg, Throwable t, Object... params);


}
