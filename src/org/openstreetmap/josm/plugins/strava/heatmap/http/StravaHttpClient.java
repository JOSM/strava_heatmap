package org.openstreetmap.josm.plugins.strava.heatmap.http;

/**
 * Interface for abstracting the HTTP-level interactions with the Strava website.
 */
public interface StravaHttpClient {

    /**
     * Sends a request to the website.
     * @param stravaHttpRequest the HTTP request.
     * @return the HTTP response.
     */
    StravaHttpResponse sendRequest(StravaHttpRequest stravaHttpRequest) throws StravaHttpException;

    /**
     * Clears the cookies store.
     * Useful for testing.
     */
    void removeAllCookiesFromCookieStore();
}
