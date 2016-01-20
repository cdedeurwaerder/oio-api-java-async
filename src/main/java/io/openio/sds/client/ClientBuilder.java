package io.openio.sds.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;

import io.openio.sds.client.settings.ProxySettings;
import io.openio.sds.client.settings.RawxSettings;
import io.openio.sds.client.settings.Settings;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Factory of {@code Client}
 * 
 *
 *
 */
public class ClientBuilder {

    private String ns;
    private String proxydUrl;
    private AsyncHttpClient http;

    /**
     * Generates a client builder to prepare {@link Client} configuration
     * 
     * @return a new {@code ClientBuilder}
     */
    public static ClientBuilder prepareClient() {
        return new ClientBuilder();
    }

    /**
     * Defines the url of the OpenIO proxyd service
     * 
     * @param proxydUrl
     *            the url to set
     * @return this
     */
    public ClientBuilder proxydUrl(String proxydUrl) {
        this.proxydUrl = proxydUrl;
        return this;
    }

    /**
     * Defines the OpenIO Namespace
     *
     * @param ns
     *          the OpenIO Namespace to set
     * @return this
     */
    public ClientBuilder ns(String ns) {
        this.ns = ns;
        return this;
    }

    /**
     * Set a specific {@link AsyncHttpClient} instance to be used by the built
     * clients
     * 
     * @param http
     *            the AsyncHttpClient instance to set
     * @return this
     */
    public ClientBuilder http(AsyncHttpClient http) {
        this.http = http;
        return this;
    }

    /**
     * Builds a client using the specified settings
     * 
     * @return the new client
     */
    public DefaultClient build() {
        checkArgument(null != ns, "Namespace cannot be null");
        checkArgument(null != proxydUrl, "Proxyd URL cannot be null");
        return new DefaultClient(null == http ? Dsl.asyncHttpClient() : http,
                new Settings().proxy(new ProxySettings()
                        .ns(ns)
                        .url(proxydUrl))
                        .rawx(new RawxSettings()));
    }

    /**
     * Creates a client without specific configuration. Useful for testing
     * purpose
     *
     * @param ns
     *            the OpenIO Namespace
     * @param proxydUrl
     *            the url of OpenIO proxyd service
     * @return The build {@link Client}
     */
    public static DefaultClient newClient(String ns, String proxydUrl) {
        return new DefaultClient(Dsl.asyncHttpClient(),
                new Settings().proxy(new ProxySettings()
                        .ns(ns)
                        .url(proxydUrl))
                        .rawx(new RawxSettings()));
    }

}
