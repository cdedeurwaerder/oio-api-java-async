package io.openio.sds.client.data;

import java.util.concurrent.LinkedBlockingQueue;

import org.asynchttpclient.request.body.generator.BodyChunk;
import org.asynchttpclient.request.body.generator.QueueBasedFeedableBodyGenerator;

/**
 * 
 *
 *
 */
public class OioQueueBasedFeedableBodyGenerator
        extends QueueBasedFeedableBodyGenerator<LinkedBlockingQueue<BodyChunk>> {

    public OioQueueBasedFeedableBodyGenerator(int qsize) {
        super(new LinkedBlockingQueue<BodyChunk>());
    }

    @Override
    protected boolean offer(BodyChunk chunk) throws Exception {
        // we need to block because we possibly work with many copies of a
        // single InputStream, we have make synchronization between writers
        // thread.
        // TODO specify a timeout and check an exit condition in case of one
        // worker crash and cannot consume its queue.        
        queue.put(chunk);
        return true;
    }

}
