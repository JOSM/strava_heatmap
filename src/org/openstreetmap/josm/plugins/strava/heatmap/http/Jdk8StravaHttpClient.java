package org.openstreetmap.josm.plugins.strava.heatmap.http;

import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StravaHttpClient} compatible with JDK 8.
 */
public class Jdk8StravaHttpClient implements StravaHttpClient {

    private static CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    public Jdk8StravaHttpClient() {
        Logging.info("Using JDK8-compatible HTTP client to connect to Strava website");
        CookieHandler.setDefault(cookieManager);
    }

    @Override
    public StravaHttpResponse sendRequest(StravaHttpRequest stravaHttpRequest) throws StravaHttpException {

        try {
            URL url = new URL(stravaHttpRequest.getUri());
            String method = stravaHttpRequest.isPost() ? "POST" : "GET";
            HttpClient httpClient = HttpClient.create(url, method);
            httpClient.setMaxRedirects(-1);

            if (stravaHttpRequest.isPost()) {
                httpClient.setHeader("Content-Type", "application/x-www-form-urlencoded");
                httpClient.setRequestBody(stravaHttpRequest.getParameters().getBytes(StandardCharsets.UTF_8));
            }
            String cookieHeader = stravaHttpRequest.getCookieHeader();
            if (!cookieHeader.isEmpty()) {
                httpClient.setHeader("Cookie", cookieHeader);
            }

            HttpClient.Response response = httpClient.connect();

            URI uri = URI.create(stravaHttpRequest.getUri());
            Map<String, String> cookies = cookieManager.getCookieStore().get(uri)
                    .stream().collect(Collectors.toMap(HttpCookie::getName, HttpCookie::getValue));

            String locationHeader = response.getHeaderField("Location");
            int statusCode = response.getResponseCode();
            String body = response.fetchContent();
            StravaHttpResponse stravaHttpResponse = new StravaHttpResponse(statusCode, body, cookies, locationHeader);

            if (Logging.isDebugEnabled()) {
                Logging.debug("HTTP status code from URI " + uri + ":\n" + statusCode);
            }
            if (Logging.isTraceEnabled()) {
                Logging.trace("HTML content from URI " + uri + ":\n" + body);
            }

            return stravaHttpResponse;
        } catch (IOException e) {
            throw new StravaHttpException(e);
        }
    }

    @Override
    public void removeAllCookiesFromCookieStore() {
        cookieManager.getCookieStore().removeAll();
    }


}
