package com.project.service;

/**
 * This class is responsible for collecting and reporting statistics
 * about the server's operations, including the total number of responses,
 * average response time, and maximum response time (time in nanoseconds).
 */
public class StatsCollector {
    private int totalResponses;
    private long totalResponseTime;
    private long maxResponseTime;

    /**
     * Default constructor for the StatsCollector. Initializes the total number of responses,
     * total response time, and maximum response time to zero.
     */
    public StatsCollector() {
        this.totalResponses = 0;
        this.totalResponseTime = 0;
        this.maxResponseTime = 0;
    }

    /**
     * Increments the total number of responses and adds the response time
     * of the current response to the total response time. If the response
     * time of the current response is larger than the current maximum response
     * time, it becomes the new maximum response time.
     *
     * @param responseTime The response time of the current response in nanoseconds.
     */
    public synchronized void addResponse(long responseTime) {
        totalResponses++;
        totalResponseTime += responseTime;
        maxResponseTime = Math.max(maxResponseTime, responseTime);
    }

    /**
     * Returns the total number of responses the server has processed.
     *
     * @return The total number of responses.
     */
    public synchronized int getTotalResponses() {
        return totalResponses;
    }

    /**
     * Calculates and returns the average response time of the server.
     * This is computed by dividing the total response time by the total number
     * of responses. If there are no responses, it returns zero.
     *
     * @return The average response time in nanoseconds.
     */
    public synchronized double getAverageResponseTimeNanoseconds() {
        if (totalResponses == 0) {
            return 0;
        }
        return (double) totalResponseTime / totalResponses;
    }

    /**
     * Returns the maximum response time the server has encountered.
     * This method is thread-safe.
     *
     * @return The maximum response time in nanoseconds.
     */
    public synchronized long getMaxResponseTimeNanoseconds() {
        return maxResponseTime;
    }
}
