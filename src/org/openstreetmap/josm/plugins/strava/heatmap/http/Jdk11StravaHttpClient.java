package org.openstreetmap.josm.plugins.strava.heatmap.http;

import org.openstreetmap.josm.tools.Logging;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StravaHttpClient} using JDK 11 HTTP client features (java.net.http).
 */
public class Jdk11StravaHttpClient implements StravaHttpClient {

    private CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    private HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(20))
            .cookieHandler(cookieManager)
            .build();

    public Jdk11StravaHttpClient() {
        Logging.info("Using JDK11-compatible HTTP client to connect to Strava website");
    }

    @Override
    public StravaHttpResponse sendRequest(StravaHttpRequest stravaHttpRequest) throws StravaHttpException
    {
        try {
            URI uri = URI.create(stravaHttpRequest.getUri());
            HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder()
                    .timeout(Duration.ofSeconds(20))
                    .uri(uri);
            if (stravaHttpRequest.isPost()) {
                httpRequestBuilder.setHeader("Content-Type", "application/x-www-form-urlencoded");
                httpRequestBuilder.POST(HttpRequest.BodyPublishers.ofString(stravaHttpRequest.getParameters()));
            }
            String cookieHeader = stravaHttpRequest.getCookieHeader();
            if (!cookieHeader.isEmpty()) {
                httpRequestBuilder.setHeader("Cookie", cookieHeader);

            }
            HttpRequest httpRequest = httpRequestBuilder.build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            Map<String, String> cookies = cookieManager.getCookieStore().get(uri)
                    .stream().collect(Collectors.toMap(HttpCookie::getName,HttpCookie::getValue));

            String locationHeader = response.headers().firstValue("Location").orElse("");
            int statusCode = response.statusCode();
            String body = response.body();
            StravaHttpResponse stravaHttpResponse = new StravaHttpResponse(statusCode, body, cookies, locationHeader);

            if (Logging.isDebugEnabled()) {
                Logging.debug("HTT" +
                        "P status code from URI " + uri + ":\n" + statusCode);
            }
            if (Logging.isTraceEnabled()) {
                Logging.trace("HTML content from URI " + uri + ":\n" + body);
            }

            return stravaHttpResponse;

        } catch (IOException | InterruptedException e) {
            throw new StravaHttpException(e);
        }
    }

    @Override
    public void removeAllCookiesFromCookieStore() {
        cookieManager.getCookieStore().removeAll();
    }
}
