package com.project.protocol.request;

import com.project.service.Computer;
import com.project.domain.variablevaluesfunction.VariableValuesFunction;
import com.project.protocol.request.ComputationRequest;
import com.project.protocol.request.Request;
import com.project.protocol.request.StatRequest;
import com.project.protocol.response.ErrorResponse;
import com.project.protocol.response.Response;
import com.project.server.ExpressionServer;
import com.project.service.StatsCollector;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class RequestHandler {

    private final StatsCollector statsCollector;
    private final ExecutorService computationThreadPool;
    private final Computer computer;

    /**
     * Constructor for the RequestHandler class.
     * @param expressionServer The server from which the thread pool and stats collector are obtained.
     */
    public RequestHandler(ExpressionServer expressionServer) {
        this.computationThreadPool = expressionServer.getComputationThreadPool();
        this.statsCollector = expressionServer.getStatsCollector();
        this.computer = expressionServer.getComputer();
    }

    /**
     * Handles incoming requests.
     * @param requestString The raw request string sent by the client.
     * @return The response object to be sent back to the client.
     */
    public Response handleRequest(String requestString) {
        long startTime = System.nanoTime();
        Response response;
        try {
            Request request = parse(requestString);
            response = request.process(startTime);

            long responseTime = System.nanoTime() - startTime;
            statsCollector.addResponse(responseTime);

        } catch (IllegalArgumentException e) {
            response = new ErrorResponse("(IllegalArgumentException) " + e.getMessage());
        }

        return response;
    }

    /**
     * Parses the request string and creates the appropriate Request object.
     * @param requestString The raw request string sent by the client.
     * @return The created Request object.
     * @throws IllegalArgumentException If the request type is not recognized.
     */
    private Request parse(String requestString) throws IllegalArgumentException {
        if (isStatRequest(requestString)) {
            return new StatRequest(requestString, statsCollector);
        } else if (isComputationRequest(requestString)) {
            return new ComputationRequest(requestString, computationThreadPool, computer);
        } else {
            throw new IllegalArgumentException("Invalid request format");
        }
    }

    /**
     * Checks if the given request string is a computation request.
     * @param requestString The raw request string sent by the client.
     * @return True if the request string is a computation request, false otherwise.
     */
    private static boolean isComputationRequest(String requestString) {
        String computationKinds = Arrays.stream(Computer.ComputationKind.values())
                .map(Computer.ComputationKind::getRequestString)
                .collect(Collectors.joining("|"));

        String valuesKinds = Arrays.stream(VariableValuesFunction.ValuesKind.values())
                .map(VariableValuesFunction.ValuesKind::getRequestString)
                .collect(Collectors.joining("|"));

        String computationRegex = "^(" + computationKinds + ")_(" + valuesKinds + ");";
        Pattern pattern = Pattern.compile(computationRegex);
        Matcher matcher = pattern.matcher(requestString);

        return matcher.find();
    }
    /**
     * Checks if the given request string is a stat request.
     * @param requestString The raw request string sent by the client.
     * @return True if the request string is a stat request, false otherwise.
     */
    private static boolean isStatRequest(String requestString) {
        String statTypes = Arrays.stream(StatRequest.StatType.values())
                .map(StatRequest.StatType::getRequestString)
                .collect(Collectors.joining("|"));

        String statRegex = "^(" + statTypes + ")$";
        Pattern pattern = Pattern.compile(statRegex);
        Matcher matcher = pattern.matcher(requestString);

        return matcher.find();
    }
}
