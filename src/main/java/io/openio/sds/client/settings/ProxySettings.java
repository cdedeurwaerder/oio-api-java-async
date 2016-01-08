package io.openio.sds.client.settings;

/**
 * 
 *
 *
 */
public class ProxySettings {

    private String url;

    public String url() {
        return url;
    }

    public ProxySettings url(String url) {
        this.url = url;
        return this;
    }
}
