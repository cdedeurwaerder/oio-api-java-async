package io.openio.sds.client;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.Future;

import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ListOptions;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.ObjectList;
import io.openio.sds.models.OioUrl;

/**
 * <p>
 * Contains all convenience methods to handle containers and objects in OpenIO
 * SDS
 * </p>
 * <p>
 * Instances of {@code Client} implementations are built with
 * {@link ClientBuilder} class. You could create a basic client by calling
 * {@link ClientBuilder#newClient(String, String)} method, specifying the OpenIO
 * namespace and OpenIO proxyd service url as argument.
 * The built client is ready to use.
 * </p>
 * <p>
 * Simple example:
 * 
 * <code>
 * Client client = ClientBuilder.newClient("OPENIO", "http://127.0.0.1:6002");
 * </code>
 * 
 * </p>
 * <p>
 * To enable more tunable client instance, you should specify some directives to
 * your {@link ClientBuilder} For example if you want to provide your own
 * AsyncHttpClient instance, you have to proceed as follow:
 * 
 * <code>
 * AsyncHttpClient http = Dsl.asyncHttpClient(
 *       new DefaultAsyncHttpClientConfig.Builder()
 *             .setRequestTimeout(10000)
 *             .setHttpClientCodecMaxChunkSize(8192 * 4));
 * Client client = ClientBuilder.prepareClient()
 *       .ns("OPENIO")
 *       .proxydUrl("http://127.0.0.1:6002")
 *       .http(http)
 *       .build();
 * </code>
 * </p>
 * 
 * 
 *
 *
 */
public interface Client {

    /**
     * Creates a container using the specified {@link OioUrl}. OioUrl are built
     * by using {@link OioUrl#url(String, String)} method, then you have to
     * specify the name of the account to use and the name of the future
     * container.
     * <p>
     * The container is available when the returned future is completed.
     * 
     * @param url
     *            the url of the container
     * @param listener
     *            the listener to use on completion or exception. Could be
     *            {@code null}.
     * @return a {@code Future<ContainerInfo>} available when the operation is
     *         completed
     */
    public Future<ContainerInfo> createContainer(OioUrl url,
            CompletionListener<ContainerInfo> listener);

    /**
     * Returns informations about the specified container
     * 
     * @param url
     *            the url of the container
     * @param listener
     *            the listener to use on completion or exception. Could be
     *            {@code null}.
     * @return a {@code Future<ContainerInfo>} available when the operation is
     *         completed
     */
    public Future<ContainerInfo> getContainerInfo(OioUrl url,
            final CompletionListener<ContainerInfo> listener);

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
    public Future<ObjectList> listContainer(OioUrl url,
            final ListOptions listOptions,
            final CompletionListener<ObjectList> listener);

    /**
     * Deletes the specified container asynchronously
     * 
     * @param url
     *            the url of the container
     * @param listener
     *            the listener of the deletion progress
     * @return a Future to check when the deletion is done
     */
    public Future<Boolean> deleteContainer(OioUrl url,
            final CompletionListener<Boolean> listener);

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the file to read the data from
     * @param listener
     * @return a Future which will contains informations about the object.
     */
    public Future<ObjectInfo> putObject(OioUrl url, long size,
            File data, CompletionListener<ObjectInfo> listener);

    /**
     * Push an object into the oio namespace
     * 
     * @param url
     *            the url of the object to create
     * @param size
     *            the size of the object
     * @param data
     *            the InputStream to read the data from
     * @param listener
     * @return a Future which will contains informations about the object.
     */
    public Future<ObjectInfo> putObject(OioUrl url, long size,
            InputStream data, CompletionListener<ObjectInfo> listener);

    /**
     * Returns informations about the specified object
     * 
     * @param url
     *            the url of the object
     * @param listener
     *            the listener to check progression
     * @return a Future which handles the {@code ObjectInfo}
     */
    public Future<ObjectInfo> getObjectInfo(OioUrl url,
            CompletionListener<ObjectInfo> listener);

    /**
     * Returns the data of the specified object
     * 
     * @param oinf
     *            the url of the object
     * @param listener
     *            the listener which handles data
     * @return a Future
     */
    public Future<Boolean> downloadObject(ObjectInfo oinf,
            DownloadListener listener);

    /**
     * Deletes the specified object
     * 
     * @param url
     *            the url of the object to delete
     * @param listener
     *            the listener to use on completion or exception. Could be
     *            {@code null}.
     * @return a Future which handles the {@code ObjectInfo}
     * 
     */
    public Future<ObjectInfo> deleteObject(OioUrl url,
            final CompletionListener<ObjectInfo> listener);
}
