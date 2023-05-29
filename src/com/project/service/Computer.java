package com.project.service;

import com.project.domain.expression.nodes.Constant;
import com.project.domain.expression.nodes.Node;
import com.project.domain.expression.nodes.Operator;
import com.project.domain.expression.nodes.Variable;
import com.project.domain.variablevaluesfunction.VariableValuesFunction;
import com.project.exceptions.ComputationException;
import com.project.exceptions.DivisionByZeroException;
import com.project.domain.expression.*;
import com.project.exceptions.NotANumberException;
import com.project.exceptions.ZeroOverZeroException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Computer {
    private static final int MIN_INDEX = 0;
    private static final int MAX_INDEX = 1;
    public enum ComputationKind {

        MIN("MIN"),
        MAX("MAX"),
        AVG("AVG"),
        COUNT("COUNT");

        private final String computationKindString;

        private static final Map<String, ComputationKind> stringToEnumMap = new HashMap<>();

        static {
            for (ComputationKind statType : ComputationKind.values()) {
                stringToEnumMap.put(statType.getRequestString(), statType);
            }
        }

        ComputationKind(String computationKindString) {
            this.computationKindString = computationKindString;
        }

        public String getRequestString() {
            return computationKindString;
        }

        public static ComputationKind fromRequestString(String computationKindString) {
            return stringToEnumMap.get(computationKindString);
        }
    }

    public Computer() {}

    /**
     * Computes the result based on the computation kind specified in the variableValuesFunction.
     *
     * @return the result of the computation
     * @throws IllegalArgumentException if an invalid computation kind is specified
     * @throws DivisionByZeroException if a division by zero is attempted
     * @throws NotANumberException if a NaN is found
     */
    public double computeResult(List<List<Double>> valueTuples, VariableValuesFunction variableValuesFunction, List<Expression> expressions) throws IllegalArgumentException, DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        ComputationKind computationKind = variableValuesFunction.getComputationKind();
        switch (computationKind) {
            case MIN:
                return computeMin(valueTuples, variableValuesFunction, expressions);
            case MAX:
                return computeMax(valueTuples, variableValuesFunction, expressions);
            case AVG:
                return computeAvg(valueTuples, variableValuesFunction, expressions);
            case COUNT:
                return valueTuples.size();
            default:
                throw new IllegalArgumentException("Unknown computation kind: " + computationKind);
        }
    }

    public static double round(double value) {
        int places = 6;

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    /**
     * Private helper function to calculate min and max of the provided expressions
     * for all tuples of variable values, for avoiding redundant code lines.
     *
     * @param valueTuples list of tuples of variable values
     * @param variableValuesFunction the function containing the variable names and their corresponding indices
     * @param expressions the expressions to compute
     * @throws DivisionByZeroException if a division by 0 is attempted
     * @throws ZeroOverZeroException if a division 0 / 0 is attempted
     * @throws NotANumberException if a NaN is found
     * @return the minimum value
     */
    private double[] computeMinMax(List<List<Double>> valueTuples, VariableValuesFunction variableValuesFunction, List<Expression> expressions) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double result;
        for (List<Double> tuple : valueTuples) {
            for (Expression expression : expressions) {
                try {
                    result = evaluateExpression(expression, variableValuesFunction, tuple);
                } catch (ZeroOverZeroException e) {
                    throw new ZeroOverZeroException("A 0 / 0 division occurred while evaluating " +
                            "the expression '" + expression.toString() + "' : " + e.getMessage());
                } catch (DivisionByZeroException e) {
                    throw new DivisionByZeroException("A division by 0 occurred while evaluating " +
                            "the expression '" + expression.toString() + "' : " + e.getMessage());
                } catch (NotANumberException e) {
                    throw new NotANumberException("NaN found while evaluating " +
                            "the expression '" + expression.toString() + "' : " + e.getMessage());
                }
                if (result > max) {
                    max = result;
                }
                if (result < min) {
                    min = result;
                }
            }
        }

        return new double[] {min, max};
    }

    /**
     * Computes the maximum value of the provided expressions for all tuples of variable values.
     *
     * @param valueTuples list of tuples of variable values
     * @param expressions the expressions to compute
     * @throws DivisionByZeroException if a division by 0 is attempted
     * @throws ZeroOverZeroException if a division 0 / 0 is attempted
     * @throws NotANumberException if a NaN is found
     * @return the maximum value
     */
    private double computeMax(List<List<Double>> valueTuples, VariableValuesFunction variableValuesFunction, List<Expression> expressions) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        double[] minMax = computeMinMax(valueTuples, variableValuesFunction, expressions);
        return minMax[MAX_INDEX]; // Max
    }
    /**
     * Computes the minimum value of the provided expressions for all tuples of variable values.
     *
     * @param valueTuples list of tuples of variable values
     * @param variableValuesFunction the function containing the variable names and their corresponding indices
     * @param expressions the expressions to compute
     * @throws DivisionByZeroException if a division by 0 is attempted
     * @throws ZeroOverZeroException if a division 0 / 0 is attempted
     * @throws NotANumberException if a NaN is found
     * @return the minimum value
     */
    private double computeMin(List<List<Double>> valueTuples, VariableValuesFunction variableValuesFunction, List<Expression> expressions) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        double[] minMax = computeMinMax(valueTuples, variableValuesFunction, expressions);
        return minMax[MIN_INDEX];
    }
    /**
     * Computes the average of the first provided expression for all tuples of variable values.
     *
     * @param valueTuples list of tuples of variable values
     * @param variableValuesFunction the function containing the variable names and their corresponding indices
     * @param expressions the expressions to compute
     * @throws DivisionByZeroException if a division by 0 is attempted
     * @throws ZeroOverZeroException if a division 0 / 0 is attempted
     * @throws NotANumberException if a NaN is found
     * @return the average value
     */
    private double computeAvg(List<List<Double>> valueTuples, VariableValuesFunction variableValuesFunction, List<Expression> expressions) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        double sum = 0;
        Expression expression = expressions.get(0);
        try {
            for (List<Double> tuple : valueTuples) {
                sum += evaluateExpression(expression, variableValuesFunction, tuple);
            }
        } catch (ZeroOverZeroException | DivisionByZeroException | NotANumberException e) {
            throw new ComputationException("An error occurred while computing AVG for " +
                    "the first expression : " + e.getMessage());
        }

        return sum / valueTuples.size();
    }

    /**
     * Evaluates a given mathematical expression for a specific tuple of variable values.
     *
     * @param expression the mathematical expression to evaluate
     * @param variableValuesFunction the function containing the variable names and their corresponding indices
     * @param tuple a tuple of variable values
     * @throws DivisionByZeroException if a division by 0 is attempted
     * @throws ZeroOverZeroException if a division 0 / 0 is attempted
     * @throws NotANumberException if a NaN is found
     * @return the result of the evaluation
     */
    private double evaluateExpression(Expression expression, VariableValuesFunction variableValuesFunction, List<Double> tuple) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        return evaluateNode(expression.getRoot(), variableValuesFunction, tuple);
    }

    /**
     * Evaluates a given node in a mathematical expression for a specific tuple of variable values.
     *
     * @param node the node to evaluate
     * @param variableValuesFunction the function containing the variable names and their corresponding indices
     * @param tuple a tuple of variable values
     * @return the result of the evaluation
     * @throws DivisionByZeroException if a division by 0 is attempted
     * @throws ZeroOverZeroException if a division 0 / 0 is attempted
     */
    private double evaluateNode(Node node, VariableValuesFunction variableValuesFunction, List<Double> tuple) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {

        // If the node is a constant, return its value
        if (node instanceof Constant) {
            return ((Constant) node).getValue();
        }

        // If the node is a variable, return the corresponding value from the tuple
        else if (node instanceof Variable) {
            String variableName = ((Variable) node).getName();
            int variableIndex = variableValuesFunction.getVariableIndex(variableName);
            return tuple.get(variableIndex);
        }

        // If the node is an operator, calculate the result of the operation
        else {
            Operator operator = (Operator) node;

            List<Node> children = operator.getChildren();

            // Evaluate the child nodes and collect the results in an array
            double[] childValues = children.stream()
                    .mapToDouble(child -> evaluateNode(child, variableValuesFunction, tuple))
                    .toArray();

            if (Double.isNaN(childValues[0]) || Double.isNaN(childValues[1])) {
                throw new NotANumberException("NaN obtained during computation");
            }
            if (operator.getType() == Operator.Type.DIVISION && childValues[1] == 0) {
                if (childValues[0] == 0) {
                    throw new ZeroOverZeroException("Undefined result at node '" + node + "'");
                } else {
                    throw new DivisionByZeroException("Division by zero at node '" + node + "'");
                }
            }

            // Apply the operator's function to the array of child values and return the result
            return operator.getType().getFunction().apply(childValues);
        }
    }

}
