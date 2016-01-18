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

	public String ns() { return ("NS"); }

	public String proxyd() {
		return ("http://127.0.0.1:6002");
	}

    // TODO provide actual test_file
    public String test_file() {
        return ("/home/cde/work/workspace/sample.tar.gz");
    }

}
