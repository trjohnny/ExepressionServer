package com.project.protocol.request;

import com.project.domain.variablevaluesfunction.VariableValuesFunctionParser;
import com.project.exceptions.ComputationException;
import com.project.exceptions.ExpressionException;
import com.project.exceptions.ExpressionParsingException;
import com.project.exceptions.VariableValuesFunctionException;
import com.project.service.Computer;
import com.project.domain.variablevaluesfunction.VariableValuesFunction;
import com.project.domain.expression.Expression;
import com.project.domain.expression.nodes.Node;
import com.project.domain.expression.ExpressionParser;
import com.project.protocol.response.ErrorResponse;
import com.project.protocol.response.OkResponse;
import com.project.protocol.response.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * The ComputationRequest class handles the processing of com.project.computation requests.
 */
public class ComputationRequest implements Request {

    private final String computationString;
    private final ExecutorService computationThreadPool;
    private final Computer computer;
    private String[] requestParts;
    private final int maxComputationTime = 10;
    private final int maxQueueTime = 120;

    /**
     * Constructs a new ComputationRequest instance.
     *
     * @param computationString the com.project.computation request string
     * @param computationThreadPool the thread pool for com.project.computation tasks
     */
    public ComputationRequest(String computationString, ExecutorService computationThreadPool, Computer computer) {
        this.computationThreadPool = computationThreadPool;
        this.computationString = computationString;
        this.computer = computer;
    }

    /**
     * Processes the com.project.computation request.
     *
     * @param startTime the start time of the com.project.computation
     * @return a Response object that represents the result of the com.project.computation
     */
    @Override
    public Response process(long startTime) {
        // Split the com.project.computation string by ";"
        this.requestParts = computationString.split(";");
        if (requestParts.length < 3) {
            return new ErrorResponse("Invalid computation request format: request parts < 3");
        }

        Future<Response> futureResponse = computationThreadPool.submit(() -> {
            FutureTask<Response> innerTask = new FutureTask<>(() -> {
                try {
                    // Parse a variable-values function a from the VariableValuesFunction part of the request
                    VariableValuesFunction variableValuesFunction = parseVariableValuesFunction();

                    // Build a list T of value tuples from a
                    List<List<Double>> valueTuples = variableValuesFunction.generateValueTuples();

                    // Parse a non-empty list E=(e1,…,en) of expressions from the Expressions part of the request
                    List<Expression> expressions = parseExpressions();

                    // Compute a value o on T and E depending on the ComputationKind part of the request
                    double computationResult = computer.computeResult(valueTuples, variableValuesFunction, expressions);

                    // If everything is successful, return an OkResponse with the computation result
                    return new OkResponse(startTime, computationResult);
                } catch (ExpressionException e) {
                    String errorMessage = "(ExpressionException) : " + e.getMessage();
                    return new ErrorResponse(errorMessage);
                } catch (VariableValuesFunctionException e) {
                    String errorMessage = "(VariableValuesFunctionException) : " + e.getMessage();
                    return new ErrorResponse(errorMessage);
                } catch (ComputationException e) {
                    String errorMessage = "(ComputationException) : " + e.getMessage();
                    return new ErrorResponse(errorMessage);
                }
            });

            Thread innerThread = new Thread(innerTask);
            innerThread.start();
            try {
                return innerTask.get(maxComputationTime, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println(e.getMessage());
                return new ErrorResponse("Can't process the request.");
            } catch (TimeoutException e) {
                innerThread.interrupt();
                String errorMessage = "(ComputationTimeoutException) : The computation took longer " +
                        "than " + maxComputationTime + " seconds.";
                return new ErrorResponse(errorMessage);
            }
        });

        try {
            return futureResponse.get(maxQueueTime, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println(e.getMessage());
            return new ErrorResponse("Can't process the request.");
        } catch (TimeoutException e) {
            String errorMessage = "(QueueTimeoutException) : The computation request stayed in the " +
                    "queue for more than " + maxQueueTime + " seconds.";
            return new ErrorResponse(errorMessage);
        }
    }
    /**
     * Parses a VariableValuesFunction object from the computation request string.
     *
     * @return a VariableValuesFunction object
     * @throws IllegalArgumentException if the variable values in the computation request string are invalid
     */
    private VariableValuesFunction parseVariableValuesFunction() throws VariableValuesFunctionException {
        VariableValuesFunctionParser parser = new VariableValuesFunctionParser();
        String[] kindParts = requestParts[0].split("_");
        if (kindParts.length != 2) {
            throw new IllegalArgumentException("Invalid computation kind or values kind format.");
        }

        String computationKindString = kindParts[0];
        String valuesKindString = kindParts[1];

        Computer.ComputationKind computationKind = Computer.ComputationKind.fromRequestString(computationKindString);
        VariableValuesFunction.ValuesKind valuesKind = VariableValuesFunction.ValuesKind.fromRequestString(valuesKindString);

        return parser.parse(requestParts[1], valuesKind, computationKind);
    }
    /**
     * Parses a list of Expression objects from the computation request string.
     *
     * @return a list of Expression objects
     * @throws ExpressionParsingException if the expressions in the computation request string are invalid
     */
    private List<Expression> parseExpressions() throws ExpressionParsingException {
        String[] expressionStrings = Arrays.copyOfRange(this.requestParts, 2, this.requestParts.length);

        List<Expression> expressions = new ArrayList<>();
        Node node;

        for (String expressionString : expressionStrings) {
            ExpressionParser expressionParser = new ExpressionParser(expressionString);
            try {
                node = expressionParser.parse();
                if (!expressionParser.isValidExpression()) {
                    throw new ExpressionParsingException("Invalid expression format.");
                }
            } catch (ExpressionParsingException e) {
                throw new ExpressionParsingException("Parsing error for " +
                        "expression '" + expressionString + "' : " + e.getMessage());
            }
            expressions.add(new Expression(node, expressionString));
        }

        return expressions;
    }


}
