package com.project.protocol.response;

import java.util.Arrays;
import java.util.stream.Collectors;
/**
 * The OkResponse class implements the Response interface and represents a successful response from the server.
 * It includes response time and any data associated with the response.
 */
public class OkResponse implements Response {

    private final double data;
    private final double responseTime;

    /**
     * Constructs a new OkResponse with the specified start time and data.
     *
     * @param startTime The start time of the request, used to calculate the response time.
     * @param data The data associated with the response.
     */
    public OkResponse(long startTime, double data) {
        this.data = data;
        this.responseTime = (System.nanoTime() - startTime) / 1_000_000_000.0;
    }

    /**
     * Returns a string representation of the OkResponse.
     * The format is "OK;responseTime;result
     *
     * @return A string representation of the OkResponse.
     */
    @Override
    public String toString() {
        return "OK;" + String.format("%.3f", responseTime) + ";" + String.format("%.6f", data);
    }
}
