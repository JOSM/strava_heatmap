package org.openstreetmap.josm.plugins.strava.heatmap.authentication;

import org.openstreetmap.josm.plugins.strava.heatmap.http.*;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Retrieves the HTTP session cookies required for accessing the high-resolution Strava heatmaps.
 * The plugin emulates the submission of the Strava website login form.
 * This is a 3-step process:
 * 1. Retrieving the CSRF token from the login form.
 * 2. Posting the form with the CSRF token together with the user email and password.
 * 3. After successful authentication, retrieving and storing the HTTP session cookies.
 * The cookies are stored in the JOSM preferences and reused as long as they are valid.
 * When the stored cookies become obsolete the authentication process is triggered again
 * and the cookies values are refreshed.
 */
public class StravaCookiesRetriever {


    private static final String BASE_URL = "https://www.strava.com";
    private static final String LOGIN_FORM_URL = BASE_URL + "/login";
    private static final String LOGIN_POST_URL = BASE_URL + "/session";
    private static final String DASHBOARD_URL = BASE_URL + "/dashboard";
    private static final String ONBOARDING_URL = BASE_URL + "/onboarding";

    private static final String HEATMAP_BASE_URL = "https://heatmap-external-a.strava.com";
    private static final String HEATMAP_AUTH_URL = HEATMAP_BASE_URL + "/auth";
    private static final String HEATMAP_TILE_URL = HEATMAP_BASE_URL + "/tiles-auth/winter/bluered/9/256/255";

    private StravaHttpClient stravaHttpClient = StravaHttpClientFactory.getStravaHttpClient();

    /**
     * Returns the heatmap authentication cookies.
     * If the cookies stored in the JOSM preferences are still valid, they are reused.
     * Otherwise the authentication process is triggered again and new cookies are retrieved from the Strava website.
     *
     * @return the cookies formatted as an HTTP "Cookie" header.
     */
    public String getCookiesAsHttpHeader() throws StravaHttpException, StravaAuthenticationException {
        String cookies = Config.getPref().get("strava.heatmap.cookies");
        if (cookies != null && !cookies.isEmpty() && areCookiesStillValid(cookies)) {
            return cookies;
        } else {
            logonStrava();
            String newCookies = retrieveHeatmapCookies();
            Config.getPref().put("strava.heatmap.cookies", newCookies);
            return newCookies;
        }
    }

    /**
     * Tests if the cookies are still valid.
     * The test consists in attempting to download an arbitrary  tile on the tile server.
     * If the tile server returns a HTTP status code 403 then the cookies are expired.
     *
     * @param cookies the cookies to be tested for validity.
     * @return true if the cookies are still valid.
     */
    public boolean areCookiesStillValid(String cookies) throws StravaHttpException {

        if (Logging.isDebugEnabled()) {
            Logging.debug("Checking authentication status on " + HEATMAP_TILE_URL);
        }

        StravaHttpRequest request = new StravaHttpRequest();
        request.setUri(HEATMAP_TILE_URL);
        request.setCookieHeader(cookies);
        StravaHttpResponse response = stravaHttpClient.sendRequest(request);

        int httpStatusCode = response.getHttpStatusCode();
        if (httpStatusCode == 200) {
            return true;
        } else if (httpStatusCode == 403) {
            return false;
        } else {
            throw new StravaHttpException("Unable to check authentication status: HTTP error code " + httpStatusCode);
        }

    }


    /**
     * Logs on the Strava website by submitting the HTML login form.
     * Upon successful authentication the web server should redirect to either the "Dashboard" or the "Onboarding" pages.
     */
    private void logonStrava() throws StravaHttpException, StravaAuthenticationException {

        String email = Config.getPref().get("strava.heatmap.email");
        if (email == null || email.isEmpty()) {
            throw new StravaAuthenticationException("Email address not set");
        }
        String password = Config.getPref().get("strava.heatmap.password");
        if (password == null || password.isEmpty()) {
            throw new StravaAuthenticationException("Password not set");
        }
        String csrfToken = retrieveCsrfToken();
        String requestParametersString = buildRequestParametersString(email, password, csrfToken);

        StravaHttpRequest request = new StravaHttpRequest();
        request.setUri(LOGIN_POST_URL);
        request.setPost(true);
        request.setParameters(requestParametersString);
        StravaHttpResponse response = stravaHttpClient.sendRequest(request);

        int httpStatusCode = response.getHttpStatusCode();
        String redirect = response.getLocationHeader();

        if (httpStatusCode != 302) {
            throw new StravaHttpException("The website returned an unexpected status " + httpStatusCode);
        } else if (LOGIN_FORM_URL.equals(redirect)) {
            // redirection back to the login form means that authentication failed
            throw new StravaAuthenticationException("Authentication failed");
        } else if (!DASHBOARD_URL.equals(redirect) && !ONBOARDING_URL.equals(redirect)) {
            throw new StravaHttpException("The website redirected to an unexpected page: " + redirect);
        }
    }

    /**
     * Retrieves the CSRF token from the HTML login form.
     * A valid CSRF token is required for submitting the form.
     *
     * @return the CSRF token.
     */
    private String retrieveCsrfToken() throws StravaHttpException {
        StravaHttpRequest request = new StravaHttpRequest();
        request.setUri(LOGIN_FORM_URL);
        StravaHttpResponse response = stravaHttpClient.sendRequest(request);
        String stravaLoginPageHtmlContent = response.getBody();
        StravaCsrfTokenParser csrfTokenParser = new StravaCsrfTokenParser(stravaLoginPageHtmlContent);
        String csrfToken = csrfTokenParser.findCsrfToken();
        if (csrfToken.isEmpty()) {
            throw new StravaHttpException("The CSRF token was not found in the login page");
        }
        return csrfToken;
    }

    /**
     * Retrieves the heatmap authentication cookies.
     * This must be performed after a successful login with {@link #logonStrava()}.
     *
     * @return the heatmap authentication cookies.
     */
    private String retrieveHeatmapCookies() throws StravaHttpException {

        StravaHttpRequest request = new StravaHttpRequest();
        request.setUri(HEATMAP_AUTH_URL);
        StravaHttpResponse response = stravaHttpClient.sendRequest(request);

        Map<String, String> cookies = response.getCookies();

        return cookies.entrySet().stream()
                .filter(cookie -> cookie.getKey().startsWith("CloudFront"))
                .map(c -> c.getKey() + "=" + c.getValue())
                .collect(Collectors.joining(";"));
    }

    /**
     * Builds the URL-encoded list of form parameters to be sent when submitting the login form.
     *
     * @param email     Strava account email.
     * @param password  Strava account password.
     * @param csrfToken CSRF token.
     * @return an URL-encoded list of form parameters.
     */
    private String buildRequestParametersString(String email, String password, String csrfToken) {
        String requestParametersString = "email=";
        requestParametersString += urlEncode(email);
        requestParametersString += "&password=";
        requestParametersString += urlEncode(password);
        requestParametersString += "&authenticity_token=";
        requestParametersString += urlEncode(csrfToken);
        requestParametersString += "&utf8=";
        requestParametersString += urlEncode("✓");
        requestParametersString += "&plan=";
        return requestParametersString;
    }

    /**
     * URL-encodes a parameter.
     * The charset is UTF-8.
     * TODO use {@link URLEncoder#encode(String, Charset)} when upgrading to JDK 11
     *
     * @param parameter the parameter to URL-encode.
     * @return the URL-encoded parameter.
     */
    private String urlEncode(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Removes all cookies from the cookie manager.
     * Useful for unit testing.
     */
    public void removeAllCookiesFromCookieStore() {
        stravaHttpClient.removeAllCookiesFromCookieStore();
    }
}
