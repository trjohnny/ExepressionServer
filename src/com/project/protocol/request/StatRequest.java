package com.project.protocol.request;
import com.project.protocol.response.OkResponse;
import com.project.protocol.response.Response;
import com.project.service.StatsCollector;
import java.util.HashMap;
import java.util.Map;

public class StatRequest implements Request {

    public enum StatType {

        STAT_REQS("STAT_REQS"),
        STAT_AVG_TIME("STAT_AVG_TIME"),
        STAT_MAX_TIME("STAT_MAX_TIME");

        private final String requestString;

        private static final Map<String, StatType> stringToEnumMap = new HashMap<>();

        static {
            for (StatType statType : StatType.values()) {
                stringToEnumMap.put(statType.getRequestString(), statType);
            }
        }

        StatType(String requestString) {
            this.requestString = requestString;
        }

        public String getRequestString() {
            return requestString;
        }

        public static StatType fromRequestString(String requestString) {
            return stringToEnumMap.get(requestString);
        }
    }

    private final StatType statType;
    private final StatsCollector statsCollector;

    /**
     * Constructs a new StatRequest with the specified stat type (built from
     * the request string) and stats collector.
     *
     * @param requestString The stat request string.
     * @param statsCollector The StatsCollector used to gather the requested statistics.
     */
    public StatRequest(String requestString, StatsCollector statsCollector) {
        this.statType = StatRequest.StatType.fromRequestString(requestString);
        this.statsCollector = statsCollector;
    }

    /**
     * Processes the statistical request and returns a response.
     * The response contains the requested statistic if the stat type is valid, or an error message otherwise.
     *
     * @param startTime The start time of the request, used to calculate the response time.
     * @return An OkResponse containing the requested statistic if the stat type is valid, or an ErrorResponse otherwise.
     */
    @Override
    public Response process(long startTime) {
        switch (statType) {
            case STAT_REQS:
                return new OkResponse(startTime, statsCollector.getTotalResponses());
            case STAT_AVG_TIME:
                return new OkResponse(startTime, statsCollector.getAverageResponseTimeNanoseconds() / 1_000_000_000.0);
            case STAT_MAX_TIME:
                return new OkResponse(startTime, statsCollector.getMaxResponseTimeNanoseconds() / 1_000_000_000.0);
            default:
                throw new IllegalArgumentException(String.format("Invalid stat type: %s", statType));
        }
    }
}
