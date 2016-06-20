package net.technolords.tools.test.domain;

/**
 * Created by Technolords on 2016-Jun-20.
 */
public class Result {
    private String response;
    private int statusCode;
    private long size;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
