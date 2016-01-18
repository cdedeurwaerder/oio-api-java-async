package io.openio.sds.client;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.net.HttpHeaders.CONTENT_LENGTH;
import static io.openio.sds.client.OioConstants.*;
import static java.lang.String.format;
import static java.nio.ByteBuffer.wrap;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.HttpResponseHeaders;
import org.asynchttpclient.HttpResponseStatus;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.asynchttpclient.request.body.generator.FileBodyGenerator;

import io.openio.sds.client.data.OioQueueBasedFeedableBodyGenerator;
import io.openio.sds.client.settings.RawxSettings;
import io.openio.sds.common.logging.Loggers;
import io.openio.sds.common.logging.SDSLogger;
import io.openio.sds.exceptions.SdsException;
import io.openio.sds.models.ChunkInfo;
import io.openio.sds.models.ObjectInfo;

/**
 * 
 *
 *
 */
public class RawxClient {

    private static final SDSLogger logger = Loggers
            .getLogger(DefaultClient.class);

    private static final int MIN_WORKERS = 1;
    private static final int MAX_WORKERS = 20;
    private static final int IDLE_THREAD_KEEP_ALIVE = 30; // in seconds
    private static final int BACKLOG_MAX_SIZE = 10 * MAX_WORKERS;

    private final AsyncHttpClient http;
    private final ExecutorService executors;
    private final RawxSettings settings;

    RawxClient(AsyncHttpClient http, RawxSettings settings) {
        this.http = http;
        this.settings = settings;
        this.executors = new ThreadPoolExecutor(
                MIN_WORKERS,
                MAX_WORKERS,
                IDLE_THREAD_KEEP_ALIVE,
                SECONDS,
                new LinkedBlockingQueue<>(BACKLOG_MAX_SIZE),
                new ThreadFactory() {

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r);
                        t.setName("RawxClient-Worker");
                        return t;
                    }
                });
    }

    public static RawxClient client(AsyncHttpClient http,
            RawxSettings settings) {
        checkArgument(null != http, "AsynHttpClient cannot be null");
        checkArgument(null != settings, "Settings cannot be null");
        return new RawxClient(http, settings);
    }

    /**
     * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
     * 
     * @param oinf
     *            the ObjectInfo to deal with
     * @param data
     *            the data to upload
     * @param listener
     *            the {@link UploadListener} to check progression
     * @return a ListenableFuture which handles the updated {@code ObjectInfo}
     */
    public CompletableFuture<ObjectInfo> uploadChunks(ObjectInfo oinf,
            InputStream data, UploadListener listener) {
        long remaining = oinf.size();
        long size = Math.min(remaining, oinf.chunksize(0));
        CompletableFuture<ObjectInfo> f = uploadPosition(oinf, 0, size, data,
                listener);
        remaining -= size;
        for (int i = 1; i < oinf.nbchunks(); i++) {
            final int pos = i;
            final long csize = Math.min(remaining, oinf.chunksize(pos));
            f = f.thenCompose(
                    o -> uploadPosition(oinf, pos, csize, data, listener));
            remaining -= csize;
        }
        return f.thenApply(o -> {
            if (null != listener)
                listener.onCompleted();
            return o;
        });
    }

    /**
     * Uploads the chunks of the specified {@code ObjectInfo} asynchronously
     * 
     * @param oinf
     *            the ObjectInfo to deal with
     * @param data
     *            the data to upload
     * @param listener
     *            the {@link UploadListener} to check progression
     * @return a ListenableFuture which handles the updated {@code ObjectInfo}
     */
    public CompletableFuture<ObjectInfo> uploadChunks(
            ObjectInfo oinf, File data, UploadListener listener) {
        List<CompletableFuture<ObjectInfo>> futures = new ArrayList<>();
        long remaining = oinf.size();
        for (int i = 0; i < oinf.sortedChunks().size(); i++) {
            long size = Math.min(remaining,
                    oinf.sortedChunks().get(i).get(0).size());
            uploadPosition(oinf, i, remaining, size, data, listener, futures);
            remaining -= size;
        }
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> {
                    if (null != listener)
                        listener.onCompleted();
                    return oinf;
                });
    }

    public List<ListenableFuture<ChunkInfo>> uploadChunks(
            List<ChunkInfo> chunks, byte[] data) {
        throw new RuntimeException("NOT YET IMPL");
    }

    public ListenableFuture<Boolean> downloadObject(ObjectInfo oinf,
            DownloadListener listener) {
        checkArgument(null != oinf);
        checkArgument(null != listener);
        LinkedList<ListenableFuture<Boolean>> futures = new LinkedList<ListenableFuture<Boolean>>();
        Semaphore completionLock = new Semaphore(0);
        downloadPosition(oinf, 0, 0, listener, futures, completionLock);
        return new ObjectDownloadFuture(futures, completionLock);
    }

    /* --- INTERNALS --- */

    private CompletableFuture<ObjectInfo> uploadPosition(ObjectInfo oinf,
            int pos, Long size, InputStream data, UploadListener listener) {
        List<CompletableFuture<ObjectInfo>> futures = new ArrayList<>();
        List<ChunkInfo> cil = oinf.sortedChunks().get(pos);
        List<OioQueueBasedFeedableBodyGenerator> gens = size == 0 ? null
                : feedableBodys(cil.size(), size);
        for (int i = 0; i < cil.size(); i++) {
            ChunkInfo ci = cil.get(i);
            BoundRequestBuilder builder = http.preparePut(ci.url())
                    .setHeader(CHUNK_META_CONTAINER_ID, oinf.url().cid())
                    .setHeader(CHUNK_META_CONTENT_ID, oinf.oid())
                    .setHeader(CHUNK_META_CONTENT_POLICY, oinf.policy())
                    .setHeader(CHUNK_META_CONTENT_MIME_TYPE, oinf.mtype())
                    .setHeader(CHUNK_META_CONTENT_CHUNK_METHOD, oinf.chunkMethod())
                    .setHeader(CHUNK_META_CONTENT_CHUNKSNB,
                            String.valueOf(oinf.nbchunks()))
                    .setHeader(CHUNK_META_CONTENT_SIZE,
                            String.valueOf(oinf.size()))
                    .setHeader(CHUNK_META_CONTENT_PATH, oinf.url().object())
                    .setHeader(CHUNK_META_CHUNK_ID, ci.id())
                    .setHeader(CHUNK_META_CHUNK_POS, ci.pos().toString())
                    .setHeader(CONTENT_LENGTH, String.valueOf(size))
                    .setHeader("Connection", "Close");
            if (null == gens)
                builder = builder.setBody(new byte[0]);
            else
                builder = builder.setBody(gens.get(i));
            futures.add(
                    builder.execute(new AsyncCompletionHandler<ObjectInfo>() {
                        @Override
                        public void onThrowable(Throwable t) {
                            listener.onThrowable(t);
                        }

                        @Override
                        public ObjectInfo onCompleted(Response response)
                                throws Exception {
                            if (200 != response.getStatusCode()
                                    && 201 != response.getStatusCode()) {
                                throw new SdsException(
                                        format("Chunk %s upload failed (%d:%s)",
                                                ci.url(),
                                                response.getStatusCode(),
                                                response.getStatusText()));
                            }
                            ci.size(size);
                            ci.hash(response.getHeader(CHUNK_META_CHUNK_HASH));
                            return oinf;
                        }
                    }).toCompletableFuture());
        }

        startConsumer(data, size, gens, futures);
        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> {
                    if (null != listener)
                        listener.onPositionCompleted(pos);
                    return oinf;
                });
    }

    private void startConsumer(InputStream data, Long size,
            List<OioQueueBasedFeedableBodyGenerator> gens,
            List<? extends Future<ObjectInfo>> futures) {
        executors.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                int done = 0;
                while (done < size) {
                    byte[] b = new byte[Math.min(size.intValue() - done,
                            settings.bufsize())];
                    try {
                        done += fill(b, data);
                        for (OioQueueBasedFeedableBodyGenerator g : gens) {
                            g.feed(wrap(b), done >= size);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        futures.stream().forEach(f -> f.cancel(true));
                        throw e;
                    }
                }
                return null;
            }

            private int fill(byte[] b, InputStream data) throws IOException {
                int done = 0;
                int read = 0;
                while (done < b.length) {
                    read = data.read(b, done, b.length - done);
                    if (-1 == read)
                        throw new EOFException("Unexpected end of stream");
                    done += read;
                }
                return done;
            }
        });
    }

    private List<OioQueueBasedFeedableBodyGenerator> feedableBodys(int count,
            long size) {
        ArrayList<OioQueueBasedFeedableBodyGenerator> res = new ArrayList<>();
        for (int i = 0; i < count; i++)
            res.add(new OioQueueBasedFeedableBodyGenerator(5));
        return res;
    }

    private void uploadPosition(ObjectInfo oinf, int pos, long remaining,
            long size, File data, UploadListener listener,
            List<CompletableFuture<ObjectInfo>> futures) {
        for (ChunkInfo ci : oinf.sortedChunks().get(pos)) {
            futures.add(http.preparePut(ci.url())
                    .setHeader(CHUNK_META_CONTAINER_ID, oinf.url().cid())
                    .setHeader(CHUNK_META_CONTENT_ID, oinf.oid())
                    .setHeader(CHUNK_META_CONTENT_POLICY, oinf.policy())
                    .setHeader(CHUNK_META_CONTENT_CHUNK_METHOD, oinf.chunkMethod())
                    .setHeader(CHUNK_META_CONTENT_MIME_TYPE, oinf.mtype())
                    .setHeader(CHUNK_META_CONTENT_CHUNKSNB,
                            String.valueOf(oinf.chunks().size()))
                    .setHeader(CHUNK_META_CONTENT_SIZE,
                            String.valueOf(oinf.size()))
                    .setHeader(CHUNK_META_CONTENT_PATH, oinf.url().object())
                    .setHeader(CHUNK_META_CHUNK_ID, ci.id())
                    .setHeader(CHUNK_META_CHUNK_POS,
                            String.valueOf(ci.pos().meta()))
                    .setHeader("Content-Length", String.valueOf(size))
                    .setBody(new FileBodyGenerator(data,
                            oinf.size() - remaining, size))
                    .execute(new AsyncCompletionHandler<ObjectInfo>() {
                        @Override
                        public ObjectInfo onCompleted(Response response)
                                throws Exception {
                            if (200 != response.getStatusCode()
                                    && 201 != response.getStatusCode()) {
                                throw new SdsException(
                                        format("Chunk %s upload failed (%d:%s)",
                                                ci.url(),
                                                response.getStatusCode(),
                                                response.getStatusText()));
                            }
                            ci.size(size);
                            ci.hash(response.getHeader(CHUNK_META_CHUNK_HASH));
                            if (null != listener)
                                listener.onPositionCompleted(ci.pos().meta());
                            return oinf;
                        }
                    }).toCompletableFuture());
        }
    }

    private void downloadPosition(ObjectInfo oinf, int pos, int retry,
            DownloadListener listener,
            LinkedList<ListenableFuture<Boolean>> futures,
            Semaphore lock) {
        if (oinf.sortedChunks().get(pos).size() < retry + 1)
            throw new SdsException(
                    String.format("Could not download chunk at pos %d", pos));
        ChunkInfo ci = oinf.sortedChunks().get(pos).get(retry);

        AsyncHandler<Boolean> handler = new AsyncHandler<Boolean>() {

            @Override
            public void onThrowable(Throwable t) {
                listener.onThrowable(t);
            }

            @Override
            public org.asynchttpclient.AsyncHandler.State onBodyPartReceived(
                    HttpResponseBodyPart bodyPart)
                            throws Exception {
                listener.onData(bodyPart.getBodyByteBuffer());
                return State.CONTINUE;
            }

            @Override
            public org.asynchttpclient.AsyncHandler.State onStatusReceived(
                    HttpResponseStatus status)
                            throws Exception {
                if (200 != status.getStatusCode()) {
                    // this chunk is unreachable for some reason, switch
                    logger.warn("Unable to download chunk (%d %s) %s",
                            status.getStatusCode(),
                            status.getStatusText(), ci.url());
                    downloadPosition(oinf, pos, retry + 1, listener,
                            futures, lock);
                    return State.ABORT;
                }
                return State.CONTINUE;
            }

            @Override
            public org.asynchttpclient.AsyncHandler.State onHeadersReceived(
                    HttpResponseHeaders headers) throws Exception {
                return State.CONTINUE;
            }

            @Override
            public Boolean onCompleted() throws Exception {
                listener.onPositionCompleted(pos);
                if (pos == oinf.sortedChunks().size() - 1) {
                    lock.release();
                    listener.onCompleted();
                } else
                    downloadPosition(oinf, pos + 1, 0, listener,
                            futures, lock);
                return true;
            }
        };

        futures.addFirst(http.prepareGet(ci.url()).execute(handler));
    }
}
