package org.openstreetmapjosm.plugins.strava.heatmap.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openstreetmap.josm.JOSMFixture;
import org.openstreetmap.josm.plugins.strava.heatmap.authentication.StravaCookiesRetriever;
import org.openstreetmap.josm.spi.preferences.Config;
import org.openstreetmap.josm.tools.Logging;


public class StravaCookiesRetrieverTest {

    /**
     * The email credential is externalized as a Java System property.
     * @return the test Strava account email.
     */
    private static String getEmail() {
        return System.getProperty("strava.heatmap.test.email");
    }

    /**
     * The password credential is externalized as a Java System property.
     * @return the test Strava account password.
     */
    private static String getPassword() {
        return System.getProperty("strava.heatmap.test.password");
    }

    /**
     * Setup test.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        Logging.setLogLevel(Logging.LEVEL_TRACE);
        JOSMFixture.createUnitTestFixture().init();
        Config.getPref().put("strava.heatmap.email", getEmail());
        Config.getPref().put("strava.heatmap.password", getPassword());


    }


    @Test
    public void retrieveCookiesTest() throws Exception{
        StravaCookiesRetriever cookiesRetriever = new StravaCookiesRetriever();
        cookiesRetriever.getCookiesAsHttpHeader();

    }

    @Test
    public void cookiesStillValidTest() throws Exception {
        StravaCookiesRetriever cookiesRetriever = new StravaCookiesRetriever();
        String cookies = cookiesRetriever.getCookiesAsHttpHeader();
        cookiesRetriever.removeAllCookiesFromCookieStore();
        boolean valid = cookiesRetriever.areCookiesStillValid(cookies);
        assertTrue("The cookies should still be valid", valid);
    }

    @Test
    public void cookiesNotValidTest() throws Exception {
        StravaCookiesRetriever cookiesRetriever = new StravaCookiesRetriever();
        String cookies = "CloudFront-Key-Pair-Id=AA;CloudFront-Signature=BB;CloudFront-Policy=CC";
        boolean valid = cookiesRetriever.areCookiesStillValid(cookies);
        assertFalse("The cookies should not be valid", valid);
    }

    @Test
    public void cookiesMissingTest() throws Exception {
        StravaCookiesRetriever cookiesRetriever = new StravaCookiesRetriever();
        boolean valid = cookiesRetriever.areCookiesStillValid("");
        assertFalse("The cookies should not be valid", valid);
    }


}