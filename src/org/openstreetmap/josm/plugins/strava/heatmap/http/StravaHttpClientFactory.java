package org.openstreetmap.josm.plugins.strava.heatmap.http;

/**
 * Factory that returns either a JDK8 or JDK11 compatible HTTP client.
 */
public class StravaHttpClientFactory {

    /**
     * @return a JDK11-compatible HTTP client if the JDK 11 HTTP client API is available,
     * falls back to a JDK8-compatible HTTP client otherwise.
     */
    public static StravaHttpClient getStravaHttpClient() {
        try {
            StravaHttpClient stravaHttpClient = Class.forName("org.openstreetmap.josm.plugins.strava.heatmap.http.Jdk11StravaHttpClient")
                    .asSubclass(StravaHttpClient.class)
                    .newInstance();
            return stravaHttpClient;
        } catch (NoClassDefFoundError | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            return new Jdk8StravaHttpClient();
        }
    }
}
