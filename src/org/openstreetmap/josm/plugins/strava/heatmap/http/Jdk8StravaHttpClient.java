package org.openstreetmap.josm.plugins.strava.heatmap.http;

import org.openstreetmap.josm.tools.HttpClient;
import org.openstreetmap.josm.tools.Logging;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Implementation of {@link StravaHttpClient} compatible with JDK 8.
 * This implementation has to override the default cookie handler that is set statically in {@link HttpClient},
 * since its default cookie policy would otherwise reject the Strava sub-domain cookies.
 * See {@link Jdk11StravaHttpClient} for a simpler implementation of the {@link StravaHttpClient} interface.
 */
public class Jdk8StravaHttpClient implements StravaHttpClient {

    /**
     * Replace the default cookie policy by a new one that accepts cookies from one Strava sub-domain to another.
     * All other cookies are processed according to the original cookie policy set in {@link HttpClient}.
     */
    public static final CookiePolicy ACCEPT_STRAVA_SUBDOMAINS = new CookiePolicy() {
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            boolean shouldAccept = false;
            String host = uri.getHost();
            String cookieDomain = cookie.getDomain();
            if (host != null
                    && cookieDomain != null
                    && host.equals("www.strava.com")
                    && cookieDomain.equals("strava.com")) {
                shouldAccept = true;
            } else {
                shouldAccept = CookiePolicy.ACCEPT_ORIGINAL_SERVER.shouldAccept(uri, cookie);
            }
            if (Logging.isLoggingEnabled(Level.INFO)) {
                Logging.info("Cookie " + cookie.getName() + (shouldAccept ? " accepted." : " rejected."));
            }
            return shouldAccept;
        }
    };

    private static CookieManager STRAVA_COOKIE_MANAGER;

    static {

        try {
            // force the initialization of the static block in HttpClient where the default cookie handler is set.
            HttpClient.create(null);
            // retrieve the default cookie handler
            CookieManager oldCookieHandler = (CookieManager) CookieHandler.getDefault();
            // replace the old handler by the new one, and reuse the cookie store
            STRAVA_COOKIE_MANAGER = new CookieManager(oldCookieHandler.getCookieStore(), ACCEPT_STRAVA_SUBDOMAINS);
            CookieHandler.setDefault(STRAVA_COOKIE_MANAGER);
        } catch (Exception e) {
            Logging.log(Logging.LEVEL_ERROR, "Unable to override default cookie handler", e);
        }
    }


    public Jdk8StravaHttpClient() {
        Logging.info("Using JDK8-compatible HTTP client to connect to Strava website");
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
            Map<String, String> cookies = STRAVA_COOKIE_MANAGER.getCookieStore().get(uri)
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
        STRAVA_COOKIE_MANAGER.getCookieStore().removeAll();
    }


}
