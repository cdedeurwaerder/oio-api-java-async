package io.openio.sds;

/**
 * 
 *
 *
 */
public class TestHelper {

	public static TestHelper INSTANCE;

	public synchronized static TestHelper instance() {
		if (null == INSTANCE) {
			INSTANCE = new TestHelper();
		}
		return INSTANCE;
	}

	public String proxyd() {
		return ("http://127.0.0.1:6002");
	}

}
