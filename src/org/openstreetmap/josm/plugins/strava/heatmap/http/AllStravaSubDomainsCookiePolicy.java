package org.openstreetmap.josm.plugins.strava.heatmap.http;

import org.openstreetmap.josm.tools.Logging;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.logging.Level;


/**
 * A cookie policy that accepts cookies from Strava domain to another.
 * For all other domains it falls back to {@link CookiePolicy#ACCEPT_ORIGINAL_SERVER}.
 */
public class AllStravaSubDomainsCookiePolicy implements CookiePolicy {

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
}
