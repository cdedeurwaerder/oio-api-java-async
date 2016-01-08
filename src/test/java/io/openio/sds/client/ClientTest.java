package io.openio.sds.client;

import static io.openio.sds.client.ClientBuilder.prepareClient;
import static io.openio.sds.models.OioUrl.url;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.openio.sds.TestHelper;
import io.openio.sds.common.logging.Loggers;
import io.openio.sds.common.logging.SDSLogger;
import io.openio.sds.exceptions.ContainerExistException;
import io.openio.sds.exceptions.ContainerNotFoundException;
import io.openio.sds.models.ContainerInfo;
import io.openio.sds.models.ObjectInfo;
import io.openio.sds.models.OioUrl;

/**
 * 
 *
 *
 */
public class ClientTest {

    private static final SDSLogger logger = Loggers.getLogger(ClientTest.class);

    private static DefaultClient client;
    private static TestHelper helper = TestHelper.instance();

    private static String ACCOUNT = "OIO_TEST";

    @BeforeClass
    public static void setup() {
        client = prepareClient().proxydUrl(helper.proxyd())
                .http(Dsl.asyncHttpClient(
                        new DefaultAsyncHttpClientConfig.Builder()
                                .setRequestTimeout(10000)
                                .setHttpClientCodecMaxChunkSize(8192 * 4)
                                .setKeepAlive(false)))
                .build();
    }

    @Test
    public void containerNominal()
            throws InterruptedException, ExecutionException {
        OioUrl url = url(ACCOUNT, UUID.randomUUID().toString());
        client.createContainer(url, new CompletionListener<ContainerInfo>() {
            @Override
            public void onThrowable(Throwable t) {
                Assert.fail(t.getMessage());
            }

            @Override
            public void onResponse(ContainerInfo obj) throws Exception {
                client.deleteContainer(url, assertListener()).get();
            }
        }).get();
    }

    @Test
    public void doubleCreateContainer()
            throws InterruptedException, ExecutionException {
        OioUrl url = url(ACCOUNT, UUID.randomUUID().toString());
        client.createContainer(url, new CompletionListener<ContainerInfo>() {

            @Override
            public void onThrowable(Throwable t) {
                Assert.fail(t.getMessage());
            }

            @Override
            public void onResponse(ContainerInfo obj) throws Exception {
                try {
                    client.createContainer(url, assertListener()).get();
                    Assert.fail();
                } catch (ExecutionException e) {
                    Assert.assertTrue(
                            e.getCause() instanceof ContainerExistException);
                } finally {
                    client.deleteContainer(url, assertListener()).get();
                }
            }
        }).get();
    }

    @Test
    public void destroyUnknownContainer()
            throws InterruptedException, ExecutionException {
        try {
            client.deleteContainer(url(ACCOUNT, UUID.randomUUID().toString()),
                    null).get();
        } catch (ExecutionException e) {
            Assert.assertTrue(
                    e.getCause() instanceof ContainerNotFoundException);
        }
    }

    @Test
    public void unknownContainerInfo()
            throws InterruptedException, ExecutionException {
        try {
            client.getContainerInfo(url(ACCOUNT, UUID.randomUUID().toString()),
                    assertListener()).get();
        } catch (ExecutionException e) {
            Assert.assertTrue(
                    e.getCause() instanceof ContainerNotFoundException);
        }
    }

    @Test
    public void putEmptyContentStream()
            throws InterruptedException, ExecutionException {
        OioUrl url = url(ACCOUNT, UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url, assertListener()).get();
        byte[] src = new byte[0];
        try {
            ObjectInfo o = client
                    .putObject(url, 0L, new ByteArrayInputStream(src), null)
                    .get();
            try {
                Assert.assertNotNull(o);
                System.out.println(o);
                checkObject(o, src);
            } finally {
                client.deleteObject(url, assertListener()).get();
            }
        } finally {
            client.deleteContainer(url, assertListener()).get();
        }
    }

    @Test
    public void putSingleChunkContentStream()
            throws InterruptedException, ExecutionException {
        for (int i = 0; i < 2; i++) {
            OioUrl url = url(ACCOUNT, UUID.randomUUID().toString(),
                    UUID.randomUUID().toString());
            client.createContainer(url, assertListener()).get();
            byte[] src = bytes(8192);
            try {
                ObjectInfo o = client.putObject(url, 8192L,
                        new ByteArrayInputStream(src), null).get();
                try {
                    Assert.assertNotNull(o);
                    checkObject(o, src);
                    System.out.println(o);
                } finally {
                    client.deleteObject(url, assertListener()).get();
                }
            } finally {
                client.deleteContainer(url, assertListener()).get();
            }
        }
    }

    @Test
    // FIXME still bogus feature
    public void putMultiChunkContentStream()
            throws InterruptedException, ExecutionException {
        OioUrl url = url(ACCOUNT, UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url, assertListener()).get();
        byte[] src = bytes(1090000);
        try {
            ObjectInfo o = client.putObject(url, 1090000L,
                    new ByteArrayInputStream(src), null).get();
            System.out.println("PUT COMPLETE");
            try {
                Assert.assertNotNull(o);
                checkObject(o, src);
                System.out.println(o);
            } finally {
                client.deleteObject(url, assertListener()).get();
            }
        } finally {
            client.deleteContainer(url, assertListener()).get();
        }
    }

    @Test
    public void putEmptyContentFile()
            throws InterruptedException, ExecutionException,
            FileNotFoundException {
        OioUrl url = url(ACCOUNT, UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url, assertListener()).get();
        try {
            ObjectInfo o = client.putObject(url, 0L,
                    new File("/home/cde/work/workspace/sample.tar.gz"), null)
                    .get();
            try {
                Assert.assertNotNull(o);
                System.out.println(o);
                checkObject(o,
                        new File("/home/cde/work/workspace/sample.tar.gz"));

            } finally {
                client.deleteObject(url, assertListener()).get();
            }
        } finally {
            client.deleteContainer(url, assertListener()).get();
        }
    }

    @Test
    public void putSingleChunkContentFile()
            throws InterruptedException, ExecutionException,
            FileNotFoundException {
        OioUrl url = url(ACCOUNT, UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url, assertListener()).get();
        try {
            ObjectInfo o = client.putObject(url, 8192L,
                    new File("/home/cde/work/workspace/sample.tar.gz"), null)
                    .get();
            try {
                Assert.assertNotNull(o);
                System.out.println(o);
                checkObject(o,
                        new File("/home/cde/work/workspace/sample.tar.gz"));
            } finally {
                client.deleteObject(url, assertListener()).get();
            }
        } finally {
            client.deleteContainer(url, assertListener()).get();
        }
    }

    @Test
    public void putMultiChunkContentFile()
            throws InterruptedException, ExecutionException,
            FileNotFoundException {
        OioUrl url = url(ACCOUNT, UUID.randomUUID().toString(),
                UUID.randomUUID().toString());
        client.createContainer(url, assertListener()).get();
        try {
            ObjectInfo o = client.putObject(url, 1090000L,
                    new File("/home/cde/work/workspace/sample.tar.gz"), null)
                    .get();
            try {
                Assert.assertNotNull(o);
                checkObject(o,
                        new File("/home/cde/work/workspace/sample.tar.gz"));
                System.out.println(o);
            } finally {
                client.deleteObject(url, assertListener()).get();
            }
        } finally {
            client.deleteContainer(url, assertListener()).get();
        }
    }

    private <T> CompletionListener<T> assertListener() {
        return new CompletionListener<T>() {

            @Override
            public void onThrowable(Throwable t) {
                Assert.fail(t.getMessage());
            }

            @Override
            public void onResponse(T obj) throws Exception {
                logger.info("Action completed " + obj);
            }
        };
    }

    public void checkObject(ObjectInfo oinf, byte[] data)
            throws InterruptedException, ExecutionException {
        checkObject(oinf, new ByteArrayInputStream(data));
    }

    public void checkObject(ObjectInfo oinf, File src)
            throws InterruptedException, ExecutionException,
            FileNotFoundException {
        FileInputStream fin = new FileInputStream(src);
        checkObject(oinf, fin);
        // } catch (IOException e) {
        // Assert.fail(e.getMessage());
        // }
    }

    public void checkObject(ObjectInfo oinf, InputStream src)
            throws InterruptedException, ExecutionException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        client.downloadObject(oinf,
                new DownloadListener() {

                    @Override
                    public void onThrowable(Throwable t) {
                        t.printStackTrace();
                        Assert.fail();
                    }

                    @Override
                    public void onData(ByteBuffer bodyPart) {
                        byte[] b = new byte[bodyPart.remaining()];
                        bodyPart.get(b);
                        try {
                            bos.write(b);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("done");
                        byte[] res = bos.toByteArray();
                        for (int i = 0; i < oinf.size(); i++)
                            try {
                                Assert.assertEquals(src.read(), res[i] & 0xFF);
                            } catch (IOException e) {
                                Assert.fail(e.getMessage());
                            }
                    }

                    @Override
                    public void onPositionCompleted(int pos) {
                        System.out.println("Position " + pos + " downloaded");
                    }
                }).get();
    }

    private byte[] bytes(long size) {
        byte[] res = new byte[(int) size];
        if (size > 0)
            new Random().nextBytes(res);
        return res;
    }
}
