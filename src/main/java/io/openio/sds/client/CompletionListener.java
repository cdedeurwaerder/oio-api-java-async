package io.openio.sds.client;

public abstract class CompletionListener<T> {

    public abstract void onResponse(T obj) throws Exception;

    public abstract void onThrowable(Throwable t);

}
