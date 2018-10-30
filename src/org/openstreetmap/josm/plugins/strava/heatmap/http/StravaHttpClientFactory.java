package org.openstreetmap.josm.plugins.strava.heatmap.http;

import java.lang.reflect.InvocationTargetException;

/**
 * Factory that returns either a JDK8 or JDK11-compatible HTTP client.
 */
public class StravaHttpClientFactory {

    /**
     * Creates a JDK11-compatible HTTP client if the JDK 11 HTTP client API is available,
     * falls back to a JDK8-compatible HTTP client otherwise.
     * @return an implementation of {@link StravaHttpClient} compatible with the Java runtime environment.
     */
    public static StravaHttpClient getStravaHttpClient() {
        try {
            return Class.forName("org.openstreetmap.josm.plugins.strava.heatmap.http.Jdk11StravaHttpClient")
                    .asSubclass(StravaHttpClient.class)
                    .getDeclaredConstructor().newInstance();
        } catch (NoClassDefFoundError | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            return new Jdk8StravaHttpClient();
        }
    }
}
