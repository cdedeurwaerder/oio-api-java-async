package io.openio.sds.client.settings;

/**
 * 
 *
 *
 */
public class ProxySettings {

    private String url;

    private String ns;

    public String url() {
        return url;
    }

    public ProxySettings url(String url) {
        this.url = url;
        return this;
    }

    public String ns() { return ns; }

    public ProxySettings ns(String ns) {
        this.ns = ns;
        return this;
    }
}
