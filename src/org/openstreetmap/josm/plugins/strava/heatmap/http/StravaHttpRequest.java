package org.openstreetmap.josm.plugins.strava.heatmap.http;

/**
 * An HTTP request to be sent to the Strava website.
 */
public class StravaHttpRequest {


    private String uri;
    private boolean isPost;
    private String cookieHeader = "";
    private String parameters = "";

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }

    public String getCookieHeader() {
        return cookieHeader;
    }

    public void setCookieHeader(String cookieHeader) {
        this.cookieHeader = cookieHeader;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
