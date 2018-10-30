package org.openstreetmapjosm.plugins.strava.heatmap.authentication;

import org.junit.Test;
import org.openstreetmap.josm.plugins.strava.heatmap.authentication.StravaCsrfTokenParser;

import static org.junit.Assert.assertEquals;

public class StravaCsrfTokenParserTest {

    @Test
    public void testParserNoMatch() {

        String htmlContent = "random content";
        StravaCsrfTokenParser parser = new StravaCsrfTokenParser(htmlContent);
        String token = parser.findCsrfToken();
        assertEquals("", token);
    }


    @Test
    public void testParserMatch()  {

        String htmlContent = "<meta name=\"csrf-token\" content=\"tokenvalue\" />";
        StravaCsrfTokenParser parser = new StravaCsrfTokenParser(htmlContent);
        String token  = parser.findCsrfToken();
        assertEquals("tokenvalue",token);

    }
}
