package io.openio.sds.client;

import static com.google.common.base.Preconditions.checkArgument;
import static io.openio.sds.client.OioConstants.ACCOUNT_HEADER;
import static io.openio.sds.client.OioConstants.CONTAINER_SYS_NAME_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_CTIME_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_HASH_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_HASH_METHOD_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_ID_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_LENGTH_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_MIME_TYPE_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_POLICY_HEADER;
import static io.openio.sds.client.OioConstants.CONTENT_META_VERSION_HEADER;
import static io.openio.sds.client.OioConstants.LIST_TRUNCATED_HEADER;
import static io.openio.sds.client.OioConstants.M2_CTIME_HEADER;
import static io.openio.sds.client.OioConstants.M2_INIT_HEADER;
import static io.openio.sds.client.OioConstants.M2_USAGE_HEADER;
import static io.openio.sds.client.OioConstants.M2_VERSION_HEADER;
import static io.openio.sds.client.OioConstants.NS_HEADER;
import static io.openio.sds.client.OioConstants.OIO_ACTION_MODE_HEADER;
import static io.openio.sds.client.OioConstants.SCHEMA_VERSION_HEADER;
import static io.openio.sds.client.OioConstants.TYPE_HEADER;
import static io.openio.sds.client.OioConstants.USER_NAME_HEADER;
import static io.openio.sds.client.OioConstants.VERSION_MAIN_ADMIN_HEADER;
import static io.openio.sds.client.OioConstants.VERSION_MAIN_ALIASES_HEADER;
import static io.openio.sds.client.OioConstants.VERSION_MAIN_CHUNKS_HEADER;
import static io.openio.sds.client.OioConstants.VERSION_MAIN_CONTENTS_HEADER;
import static io.openio.sds.client.OioConstants.VERSION_MAIN_PROPERTIES_HEADER;
import static io.openio.sds.common.HttpHelper.ensureSuccess;
import static io.openio.sds.common.HttpHelper.longHeader;
import static io.openio.sds.common.JsonUtils.gson;
import static java.lang.String.format;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;

import io.openio.sds.client.request.BeansRequest;
import io.openio.sds.client.settings.ProxySettings;
import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;

/**
 * Client of the proxyd service
 * 
 *
 *
 *
 */
public class ProxyClient {

    private final AsyncHttpClient http;
    private final ProxySettings settings;

    ProxyClient(AsyncHttpClient http, ProxySettings settings) {
        this.http = http;
        this.settings = settings;
    }

    public static ProxyClient client(AsyncHttpClient http, ProxySettings settings) {
        checkArgument(null != http, "AsynHttpClient cannot be null");
        checkArgument(null != settings, "ProxySettings cannot be null");
        return new ProxyClient(http, settings);
    }

    /**
     * Creates a container for the specified {@code Account}
     * 
     * @param accountName
     *            the name of the linked {@code Account}
     * @param containerName
     *            the name of the container to create
     * @param listener
     *            the listener to use on completion or exception. Could be
     *            {@code null}.
     * @return a {@code ListenableFuture<ContainerInfo>} available when the operation
     *         is completed
     */
    public ListenableFuture<ContainerInfo> createContainer(OioUrl url,
            final CompletionListener<ContainerInfo> listener) {
        AsyncCompletionHandler<ContainerInfo> handler = new AsyncCompletionHandler<ContainerInfo>() {
            @Override
            public ContainerInfo onCompleted(Response response)
                    throws Exception {
                ensureSuccess(response);
                if (201 == response.getStatusCode())
                    throw new ContainerExistException(
                            String.format("%s already exists", url.toString()));
                ContainerInfo info = new ContainerInfo(url.container());
                if (null != listener)
                    listener.onResponse(info);
                return info;
            }

            @Override
            public void onThrowable(Throwable t) {
                if (null != listener)
                    listener.onThrowable(t);
            }
        };
        return http.preparePost(
                format("%s/v3.0/NS/container/create?acct=%s&ref=%s",
                        settings.url(), url.account(), url.container()))
                .addHeader(OIO_ACTION_MODE_HEADER, "autocreate")
                .execute(handler);
    }

    /**
     * Returns informations about the specified container
     * 
     * @param accountName
     * @param containerName
     * @param listener
     * @return
     */
    public ListenableFuture<ContainerInfo> getContainerInfo(OioUrl url,
            final CompletionListener<ContainerInfo> listener) {
        AsyncCompletionHandler<ContainerInfo> handler = new AsyncCompletionHandler<ContainerInfo>() {
            @Override
            public ContainerInfo onCompleted(Response response)
                    throws Exception {
                ensureSuccess(response);
                ContainerInfo containerInfo = new ContainerInfo(
                        url.container());
                fillContainerInfo(containerInfo, response);
                if (null != listener)
                    listener.onResponse(containerInfo);
                return containerInfo;
            }

            @Override
            public void onThrowable(Throwable t) {
                if (null != listener)
                    listener.onThrowable(t);
            }

            private void fillContainerInfo(ContainerInfo ci,
                    Response r) {
                ci.account(r.getHeader(ACCOUNT_HEADER));
                ci.ctime(longHeader(r, M2_CTIME_HEADER));
                ci.init(longHeader(r, M2_INIT_HEADER));
                ci.usage(longHeader(r, M2_USAGE_HEADER));
                ci.version(longHeader(r, M2_VERSION_HEADER));
                ci.id(r.getHeader(CONTAINER_SYS_NAME_HEADER));
                ci.ns(r.getHeader(NS_HEADER));
                ci.type(r.getHeader(TYPE_HEADER));
                ci.user(r.getHeader(USER_NAME_HEADER));
                ci.schemavers(r.getHeader(SCHEMA_VERSION_HEADER));
                ci.versionMainAdmin(r.getHeader(VERSION_MAIN_ADMIN_HEADER));
                ci.versionMainAliases(r.getHeader(VERSION_MAIN_ALIASES_HEADER));
                ci.versionMainChunks(r.getHeader(VERSION_MAIN_CHUNKS_HEADER));
                ci.versionMainContents(
                        r.getHeader(VERSION_MAIN_CONTENTS_HEADER));
                ci.versionMainProperties(
                        r.getHeader(VERSION_MAIN_PROPERTIES_HEADER));
            }
        };

        return http.prepareGet(
                format("%s/v3.0/NS/container/show?acct=%s&ref=%s",
                        settings.url(), url.account(), url.container()))
                .execute(handler);
    }

    /**
     * List object available in the specified container
     * 
     * @param url
     *            the url of the container
     * @param listOptions
     *            the listing option
     * @param listener
     *            the listener to call on completion
     * @return the Future which handle the ObjectList
     */
    public ListenableFuture<ObjectList> listContainer(OioUrl url,
            final ListOptions listOptions,
            final CompletionListener<ObjectList> listener) {
        AsyncCompletionHandler<ObjectList> handler = new AsyncCompletionHandler<ObjectList>() {
            @Override
            public ObjectList onCompleted(Response response) throws Exception {
                ensureSuccess(response);
                ObjectList objectList = gson().fromJson(
                        response.getResponseBody(),
                        ObjectList.class);
                if (null == objectList.prefixes()) {
                    objectList.setPrefixes(new ArrayList<String>(0));
                }
                objectList.setListOptions(listOptions);
                String truncated = response
                        .getHeader(LIST_TRUNCATED_HEADER);
                objectList.setTruncated(Boolean.parseBoolean(truncated));
                listener.onResponse(objectList);
                return objectList;
            }

            @Override
            public void onThrowable(Throwable t) {
                t.printStackTrace();
                listener.onThrowable(t);
            }
        };
        BoundRequestBuilder builder = http.prepareGet(
                format("%s/v3.0/NS/container/list?acct=%s&ref=%s",
                        settings.url(), url.account(), url.container()))
                .addQueryParam("max", String.valueOf(listOptions.getLimit()));
        if (null != listOptions.getPrefix())
            builder.addQueryParam("prefix", String.valueOf(listOptions.getPrefix()));
        if (null != listOptions.getMarker())
            builder.addQueryParam("marker", String.valueOf(listOptions.getMarker()));
        if (null != listOptions.getDelimiter())
            builder.addQueryParam("delimiter", String.valueOf(listOptions.getDelimiter()));
        return builder.execute(handler);
    }

    /**
     * Deletes the specified container asynchronously
     * 
     * @param url
     *            the url of the container
     * @param listener
     *            the listener of the deletion progress
     * @return a ListenableFuture to check when the deletion is done
     */
    public ListenableFuture<Boolean> deleteContainer(OioUrl url,
            final CompletionListener<Boolean> listener) {
        AsyncCompletionHandler<Boolean> handler = new AsyncCompletionHandler<Boolean>() {
            @Override
            public Boolean onCompleted(Response response)
                    throws Exception {
                ensureSuccess(response);
                if (null != listener)
                    listener.onResponse(true);
                return true;
            }

            @Override
            public void onThrowable(Throwable t) {
                if (null != listener)
                    listener.onThrowable(t);
            }
        };
        return http.preparePost(
                format("%s/v3.0/NS/container/destroy?acct=%s&ref=%s",
                        settings.url(), url.account(), url.container()))
                .execute(handler);

    }

    /**
     * Prepares an object upload by validating the name and get availables
     * chunks on the oio namespace
     * 
     * @param url
     *            the url of the object you want to upload
     * @param size
     *            the size of the future object
     * @param listener
     *            the listener to check operation progression
     * @return a ListenableFuture which handles {@code ObjectInfo}
     */
    public ListenableFuture<ObjectInfo> getBeans(OioUrl url, long size,
            final CompletionListener<ObjectInfo> listener) {
        AsyncCompletionHandler<ObjectInfo> handler = new AsyncCompletionHandler<ObjectInfo>() {
            @Override
            public ObjectInfo onCompleted(Response response) throws Exception {
                ensureSuccess(response);
                ObjectInfo objectInfo = fillObjectInfo(url, response)
                        .chunks(bodyChunk(response));
                if (null != listener)
                    listener.onResponse(objectInfo);
                return objectInfo;
            }

            @Override
            public void onThrowable(Throwable t) {
                if (null != listener)
                    listener.onThrowable(t);
            }
        };
        return http.preparePost(
                String.format(
                        "%s/v3.0/NS/content/prepare?acct=%s&ref=%s&path=%s",
                        settings.url(), url.account(), url.container(),
                        url.object()))
                .setBody(gson().toJson(new BeansRequest().size(size)))
                .execute(handler);
    }

    /**
     * Commit an object into the oio namespace
     * 
     * @param objectInfo
     *            informations about the Object to commit
     * @param listener
     * @return
     */
    public ListenableFuture<ObjectInfo> putObject(ObjectInfo objectInfo,
            final CompletionListener<ObjectInfo> listener) {
        AsyncCompletionHandler<ObjectInfo> handler = new AsyncCompletionHandler<ObjectInfo>() {
            @Override
            public ObjectInfo onCompleted(Response response) throws Exception {
                ensureSuccess(response);
                if (null != listener)
                    listener.onResponse(objectInfo);
                return objectInfo;
            }

            @Override
            public void onThrowable(Throwable t) {
                if (null != listener)
                    listener.onThrowable(t);
            }
        };
        return http
                .preparePost(String.format(
                        "%s/v3.0/NS/content/create?acct=%s&ref=%s&path=%s",
                        settings.url(), objectInfo.url().account(),
                        objectInfo.url().container(),
                        objectInfo.url().object()))
                .setHeader(CONTENT_META_LENGTH_HEADER,
                        String.valueOf(objectInfo.size()))
                .setHeader(CONTENT_META_HASH_HEADER, objectInfo.hash())
                .setBody(gson().toJson(objectInfo.chunks()))
                .execute(handler);
    }

    /**
     * Returns informations about the specified object
     * 
     * @param url
     *            the url of the object
     * @param listener
     *            the listener to check progression
     * @return a ListenableFuture which handles the {@code ObjectInfo}
     */
    public ListenableFuture<ObjectInfo> getObjectInfo(OioUrl url,
            final CompletionListener<ObjectInfo> listener) {
        AsyncCompletionHandler<ObjectInfo> handler = new AsyncCompletionHandler<ObjectInfo>() {
            @Override
            public ObjectInfo onCompleted(Response response) throws Exception {
                ensureSuccess(response);
                ObjectInfo objectInfo = fillObjectInfo(url, response)
                        .chunks(bodyChunk(response));
                listener.onResponse(objectInfo);
                return objectInfo;
            }

            @Override
            public void onThrowable(Throwable t) {
                listener.onThrowable(t);
            }
        };
        return http.prepareGet(
                String.format("%s/v3.0/NS/content/show?acct=%s&ref=%s&path=%s",
                        settings.url(), url.account(), url.container(),
                        url.object()))
                .execute(handler);
    }
    

    public ListenableFuture<ObjectInfo> deleteObject(OioUrl url,
            final CompletionListener<ObjectInfo> listener) {
        AsyncCompletionHandler<ObjectInfo> handler = new AsyncCompletionHandler<ObjectInfo>() {
            @Override
            public ObjectInfo onCompleted(Response response) throws Exception {
                ensureSuccess(response);
                ObjectInfo objectInfo = new ObjectInfo();
                if (null != listener)
                    listener.onResponse(objectInfo);
                return objectInfo;
            }

            @Override
            public void onThrowable(Throwable t) {
                if (null != listener)
                    listener.onThrowable(t);
            }
        };
        return http.preparePost(
                String.format(
                        "%s/v3.0/NS/content/delete?acct=%s&ref=%s&path=%s",
                        settings.url(), url.account(), url.container(),
                        url.object()))
                .execute(handler);
    }

    private ObjectInfo fillObjectInfo(OioUrl url, Response r) {
        return new ObjectInfo()
                .url(url)
                .oid(r.getHeader(CONTENT_META_ID_HEADER))
                .size(longHeader(r, CONTENT_META_LENGTH_HEADER))
                .ctime(longHeader(r, CONTENT_META_CTIME_HEADER))
                .policy(r.getHeader(CONTENT_META_POLICY_HEADER))
                .version(longHeader(r, CONTENT_META_VERSION_HEADER))
                .hashMethod(r.getHeader(CONTENT_META_HASH_METHOD_HEADER))
                .mtype(r.getHeader(CONTENT_META_MIME_TYPE_HEADER));
    }

    private List<ChunkInfo> bodyChunk(Response response)
            throws JsonSyntaxException, IOException {
        return gson().fromJson(response.getResponseBody(),
                new TypeToken<List<ChunkInfo>>() {
                    private static final long serialVersionUID = 1L;
                }.getType());
    }
}
