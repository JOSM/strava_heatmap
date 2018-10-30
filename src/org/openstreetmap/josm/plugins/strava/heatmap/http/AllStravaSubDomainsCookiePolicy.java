package org.openstreetmap.josm.plugins.strava.heatmap.http;

import org.openstreetmap.josm.tools.Logging;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;


/**
 * A cookie policy that accepts cookies from one Strava domain to another.
 * For all other domains it falls back to {@link CookiePolicy#ACCEPT_ORIGINAL_SERVER}.
 */
public class AllStravaSubDomainsCookiePolicy implements CookiePolicy {

    @Override
    public boolean shouldAccept(URI uri, HttpCookie cookie) {
        boolean shouldAccept;
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
        if (Logging.isDebugEnabled()) {
            Logging.debug("Cookie " + cookie.getName() + (shouldAccept ? " accepted." : " rejected."));
        }
        return shouldAccept;
    }
}
