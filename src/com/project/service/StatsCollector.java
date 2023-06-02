package com.project.service;

public class StatsCollector {
    private int totalResponses;
    private long totalResponseTime;
    private long maxResponseTime;
    public StatsCollector() {
        this.totalResponses = 0;
        this.totalResponseTime = 0;
        this.maxResponseTime = 0;
    }
    public synchronized void addResponse(long responseTime) {
        totalResponses++;
        totalResponseTime += responseTime;
        maxResponseTime = Math.max(maxResponseTime, responseTime);
    }
    public synchronized int getTotalResponses() {
        return totalResponses;
    }
    public synchronized double getAverageResponseTimeNanoseconds() {
        if (totalResponses == 0) {
            return 0;
        }
        return (double) totalResponseTime / totalResponses;
    }
    public synchronized long getMaxResponseTimeNanoseconds() {
        return maxResponseTime;
    }
}
