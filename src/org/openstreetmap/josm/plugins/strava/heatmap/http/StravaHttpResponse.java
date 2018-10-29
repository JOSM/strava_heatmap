package org.openstreetmap.josm.plugins.strava.heatmap.http;

import java.util.Map;

/**
 * An HTTP response sent by the Strava website.
 */
public class StravaHttpResponse {

    public StravaHttpResponse(int httpStatus, String body, Map<String,String> cookies) {
        this(httpStatus,body,cookies,"");
    }

    public StravaHttpResponse(int httpStatus, String body, Map<String,String> cookies, String locationHeader) {
        this.httpStatusCode = httpStatus;
        this.body=body;
        this.cookies=cookies;
        this.locationHeader=locationHeader;
    }

    private int httpStatusCode;
    private String body;
    private Map<String,String> cookies;
    private String locationHeader;


    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public String getLocationHeader() {
        return locationHeader;
    }



}
