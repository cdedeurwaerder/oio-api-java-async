package io.openio.sds.client;

/**
 * Listener for Data upload management
 * 
 *
 *
 */
public abstract class UploadListener {

	/**
	 * Called when all chunks at a specified position are successfully uploaded
	 * 
	 * @param pos
	 *            the position uploaded
	 */
	public abstract void onPositionCompleted(int pos);

	/**
	 * Called when the whole chunks of an object are successfully uploaded (even
	 * copy chunks and parity chunks in case of duplication or rain).
	 */
	public abstract void onCompleted();

	/**
	 * Called on exception raise during data upload
	 * 
	 * @param t
	 *            the raised throwable
	 */
	public abstract void onThrowable(Throwable t);
}
