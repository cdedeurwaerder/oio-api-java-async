package io.openio.sds.common;

import java.io.IOException;

import org.asynchttpclient.Response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.openio.sds.client.Error;
import io.openio.sds.common.logging.Loggers;
import io.openio.sds.common.logging.SDSLogger;

/**
 * 
 *
 *
 */
public class HttpHelper {

    private static final SDSLogger logger = Loggers.getLogger(HttpHelper.class);
    
    public static void ensureSuccess(Response response)
            throws JsonSyntaxException, IOException {
        switch (response.getStatusCode()) {
        case 100:
        case 200:
        case 201:
        case 202:
        case 204:
            return;
        default:
            if (logger.isInfoEnabled())
                logger.info(String.format("Response (%d %s) %s",
                        response.getStatusCode(),
                        response.getStatusText(),
                        response.getResponseBody()));
            throw new Gson()
                    .fromJson(response.getResponseBody(), Error.class)
                    .ex();
        }
    }
    

    public static Long longHeader(Response r, String header) {
        try {
            return Long.parseLong(r.getHeader(header));
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

}
