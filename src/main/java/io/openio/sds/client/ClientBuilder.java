package io.openio.sds.client;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;

import io.openio.sds.client.settings.ProxySettings;
import io.openio.sds.client.settings.RawxSettings;
import io.openio.sds.client.settings.Settings;

/**
 * Factory of {@code Client}
 * 
 *
 *
 */
public class ClientBuilder {

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
     * Defines the url of the Oio proxyd service
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
        return new DefaultClient(null == http ? Dsl.asyncHttpClient() : http,
                new Settings().proxy(new ProxySettings()
                        .url(proxydUrl))
                        .rawx(new RawxSettings()));
    }

    /**
     * Creates a client without specific configuration. Useful for testing
     * purpose
     * 
     * @param proxydUrl
     *            the url of Oio Proxyd service
     * @return The build {@link Client}
     */
    public static DefaultClient newClient(String proxydUrl) {
        return new DefaultClient(Dsl.asyncHttpClient(),
                new Settings().proxy(new ProxySettings()
                        .url(proxydUrl))
                        .rawx(new RawxSettings()));
    }

}
