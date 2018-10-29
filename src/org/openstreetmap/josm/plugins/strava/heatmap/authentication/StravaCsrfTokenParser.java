package org.openstreetmap.josm.plugins.strava.heatmap.authentication;

import org.openstreetmap.josm.tools.Logging;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Utility class for extracting thr CSRF token from the login page.
 */
public class StravaCsrfTokenParser {

    private static final String REGEX_CSRF_TOKEN = "\\<meta name\\=\\\"csrf-token\\\" content\\=\\\"(.*?)\\\" /\\>";
    private static final Pattern PATTERN_CSRF_TOKEN = Pattern.compile(REGEX_CSRF_TOKEN);

    private String htmlContent;

    public StravaCsrfTokenParser(String htmlContent) {
        this.htmlContent = htmlContent;
    }


    /**
     * Extract the CSRF token from the provided HTML content string.
     *
     * @return the CSRF token or an empty string if the token is not found..
     */
    public String findCsrfToken() {

        Matcher matcher = PATTERN_CSRF_TOKEN.matcher(htmlContent);

        if (matcher.find()) {

            String token = matcher.group(1);

            if (Logging.isDebugEnabled()) {
                Logging.debug("Found tag <meta>" +
                                " \"%s\" starting at " +
                                "index %d and ending at index %d.%n",
                        matcher.group(),
                        matcher.start(),
                        matcher.end());
                Logging.debug("Found CSRF token : " + token);

            }
            return token;
        } else {
            return "";
        }
    }
}
