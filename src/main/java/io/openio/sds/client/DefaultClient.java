package io.openio.sds.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Future;

import org.asynchttpclient.AsyncHttpClient;

import io.openio.sds.client.settings.Settings;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;

/**
 * Default implementation of OpenIO SDS client using {@link AsyncHttpClient}
 * 
 *
 *
 */
public class DefaultClient implements Client {

    private final ProxyClient proxy;
    private final RawxClient rawx;

    DefaultClient(AsyncHttpClient http, Settings settings) {
        this.proxy = new ProxyClient(http, settings.proxy());
        this.rawx = new RawxClient(http, settings.rawx());
    }

    public ProxyClient proxy() {
        return proxy;
    }

    @Override
    public Future<ContainerInfo> createContainer(OioUrl url,
            CompletionListener<ContainerInfo> listener) {
        checkArgument(null != url, "url cannot be null");
        return proxy.createContainer(url, listener);
    }

    @Override
    public Future<ContainerInfo> getContainerInfo(OioUrl url,
            CompletionListener<ContainerInfo> listener) {
        checkArgument(null != url, "url cannot be null");
        return proxy.getContainerInfo(url, listener);
    }

    @Override
    public Future<ObjectList> listContainer(OioUrl url, ListOptions listOptions,
            CompletionListener<ObjectList> listener) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != listOptions, "listOptions cannot be null");
        return proxy.listContainer(url, listOptions, listener);
    }

    @Override
    public Future<Boolean> deleteContainer(OioUrl url,
            CompletionListener<Boolean> listener) {
        checkArgument(null != url, "url cannot be null");
        return proxy.deleteContainer(url, listener);
    }

    @Override
    public Future<ObjectInfo> putObject(OioUrl url, long size, File data,
            CompletionListener<ObjectInfo> listener) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        return proxy.getBeans(url, size, null).toCompletableFuture()
                .thenCompose(o -> rawx.uploadChunks(o, data, null))
                .thenCompose(o -> proxy.putObject(o, listener)
                        .toCompletableFuture());
    }

    @Override
    public Future<ObjectInfo> putObject(OioUrl url, long size, InputStream data,
            CompletionListener<ObjectInfo> listener) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        return proxy.getBeans(url, size, null).toCompletableFuture()
                .thenCompose(o -> rawx.uploadChunks(o, data, null))
                .thenCompose(o -> proxy.putObject(o, listener)
                        .toCompletableFuture());
    }

    @Override
    public Future<ObjectInfo> getObjectInfo(OioUrl url,
            CompletionListener<ObjectInfo> listener) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        return proxy.getObjectInfo(url, listener);
    }

    @Override
    public Future<Boolean> downloadObject(ObjectInfo oinf,
            DownloadListener listener) {
        checkArgument(null != oinf, "ObjectInfo cannot be null");
        return rawx.downloadObject(oinf, listener);
    }

    @Override
    public Future<ObjectInfo> deleteObject(OioUrl url,
            CompletionListener<ObjectInfo> listener) {
        checkArgument(null != url, "url cannot be null");
        checkArgument(null != url.object(), "url object cannot be null");
        return proxy.deleteObject(url, listener);
    }
}
