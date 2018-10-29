package org.openstreetmap.josm.plugins.strava.heatmap.authentication;

/**
 * Exception thrown when the authentication process fails.
 * This is most likely due to invalid credentials.
 */
public class StravaAuthenticationException extends Exception {

    public StravaAuthenticationException(String message) {
        super(message);
    }
}
