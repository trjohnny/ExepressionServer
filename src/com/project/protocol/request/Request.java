package com.project.protocol.request;
import com.project.protocol.response.Response;
/**
 * The Request interface defines the contract for all types of requests that
 * can be sent to the server. All classes that implement this interface
 * represent a specific type of request.
 *
 * The Request interface provides a single method, `process()`, that must be
 * implemented by any class that implements the interface. The `process()`
 * method takes the start time of the request as input and returns a
 * Response object as output, which is the result of processing the request.
 */
public interface Request {

    /**
     * Processes the request and produces a corresponding response.
     * The specific response depends on the type of request.
     *
     * @param startTime The start time of the request, used to calculate the response time.
     * @return The response produced by processing the request.
     */
    Response process(long startTime);
}

