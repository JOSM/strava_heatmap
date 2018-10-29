package org.openstreetmap.josm.plugins.strava.heatmap.http;

/**
 * Exception thrown when the plugin cannot initiate the authentication process.
 * This may be caused by temporary network issues.
 */
public class StravaHttpException extends Exception{

    public StravaHttpException(Throwable t) {
        super(t);
    }

    public StravaHttpException(String message) {
        super(message);
    }
}
