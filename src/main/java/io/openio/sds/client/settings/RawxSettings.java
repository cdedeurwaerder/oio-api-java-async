package io.openio.sds.client.settings;

/**
 * 
 *
 *
 */
public class RawxSettings {

    public static final int DEFAULT_BUFSIZE = 32768;
    
    private int bufsize = DEFAULT_BUFSIZE;
    
    public int bufsize(){
        return bufsize;
    }
}
