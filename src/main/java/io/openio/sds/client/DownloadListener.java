package io.openio.sds.client;

import java.nio.ByteBuffer;

/**
 * 
 *
 *
 */
public interface DownloadListener {

	
	/**
	 * 
	 */
	public void onData(ByteBuffer bodyPart);
	
	/**
	 * 
	 * @param t
	 */
	public void onThrowable(Throwable t);
	
	/**
	 * Called when the whole content has been download
	 */
	public void onCompleted();
	
	/**
	 * Called when a chunk at the specified position has been fully download 
	 * @param pos the pos download
	 */
	public void onPositionCompleted(int pos);
	
	
}
