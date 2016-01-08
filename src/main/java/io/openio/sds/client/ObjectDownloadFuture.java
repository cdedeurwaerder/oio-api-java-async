package io.openio.sds.client;

import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.asynchttpclient.ListenableFuture;

import com.google.common.base.Stopwatch;

/**
 * 
 *
 *
 */
public class ObjectDownloadFuture implements ListenableFuture<Boolean> {

	private LinkedList<ListenableFuture<Boolean>> futures;

	private Semaphore lock;

	public ObjectDownloadFuture(LinkedList<ListenableFuture<Boolean>> futures,
			Semaphore lock) {
		this.futures = futures;
		this.lock = lock;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		boolean res = true;
		for (ListenableFuture<Boolean> f : futures)
			res &= f.cancel(mayInterruptIfRunning);
		return res;
	}

	@Override
	public boolean isCancelled() {
		for (ListenableFuture<Boolean> f : futures) {
			if (!f.isCancelled())
				return false;
		}
		return true;
	}

	@Override
	public boolean isDone() {
		for (ListenableFuture<Boolean> f : futures) {
			if (!f.isDone())
				return false;
		}
		return true;
	}

	@Override
	public Boolean get() throws InterruptedException, ExecutionException {
		if (null != lock) {
			lock.acquire();
			lock.release();
			return true;
		}
		for(ListenableFuture<Boolean> f : futures)
			f.get();
		return true;
	}

	@Override
	public Boolean get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		if (null != lock) {
			if (lock.tryAcquire(timeout, unit)) {
				lock.release();
				return true;
			}
		} else {
			Stopwatch sw = Stopwatch.createStarted();
			for (ListenableFuture<Boolean> f : futures) {
				long remaining = timeout - sw.elapsed(unit);
				if (remaining > 0) {
					if (null == f.get(remaining, unit))
						return null;
				} else
					return null;
			}
		}

		return true;
	}

	@Override
	public void done() {
		for (ListenableFuture<Boolean> f : futures)
			f.done();
	}

	@Override
	public void abort(Throwable t) {
		for (ListenableFuture<Boolean> f : futures) {
			f.abort(t);
		}
	}

	@Override
	public void touch() {
		for (ListenableFuture<Boolean> f : futures) {
			f.touch();
		}
	}

	@Override
	public ListenableFuture<Boolean> addListener(Runnable listener,
			Executor exec) {
		// attach to latest
		return futures.getFirst().addListener(listener, exec);
	}

	@Override
	public CompletableFuture<Boolean> toCompletableFuture() {
		// TODO Auto-generated method stub
		return null;
	}
}
