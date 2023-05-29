package com.project.protocol.request;
import com.project.protocol.response.Response;
/**
 * The Request interface represents a request to the server.
 * It includes a method to process the request and produce a corresponding response.
 */
public interface Request {

    /**
     * Processes the request and produces a corresponding response.
     *
     * @param startTime The start time of the request, used to calculate the response time.
     * @return The response produced by processing the request.
     */
    Response process(long startTime);
}

