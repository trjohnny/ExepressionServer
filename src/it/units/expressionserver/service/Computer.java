package it.units.expressionserver.service;

import it.units.expressionserver.domain.expression.Expression;
import it.units.expressionserver.domain.expression.nodes.Constant;
import it.units.expressionserver.domain.expression.nodes.Node;
import it.units.expressionserver.domain.expression.nodes.Operator;
import it.units.expressionserver.domain.expression.nodes.Variable;
import it.units.expressionserver.domain.variablevaluesfunction.VariableValuesFunction;
import it.units.expressionserver.exceptions.ComputationException;
import it.units.expressionserver.exceptions.DivisionByZeroException;
import it.units.expressionserver.exceptions.NotANumberException;
import it.units.expressionserver.exceptions.ZeroOverZeroException;

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
    /**
     * The Computer class is implemented as a single instance in this program, even though it's stateless.
     * This design choice is driven by the potential future need to maintain some state in the Computer.
     * If such need arises, having a single instance will prevent the need for refactoring existing code.
     */
    public Computer() {}

    /**
     * Computes the result based on the computation kind specified in the variableValuesFunction.
     *
     * @return the result of the computation
     * @throws DivisionByZeroException if a division by 0 is attempted
     * @throws ZeroOverZeroException if a division 0 / 0 is attempted
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

    /**
     * Private helper function to calculate min and max of the provided expressions
     * for all tuples of variable values, for avoiding redundant code lines.
     *
     * @param valueTuples list of tuples of variable values
     * @param variableValuesFunction the function containing the variable names and their corresponding indices
     * @param expressions the expressions to compute
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
    private double computeMax(List<List<Double>> valueTuples, VariableValuesFunction variableValuesFunction, List<Expression> expressions) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        double[] minMax = computeMinMax(valueTuples, variableValuesFunction, expressions);
        return minMax[MAX_INDEX]; // Max
    }
    private double computeMin(List<List<Double>> valueTuples, VariableValuesFunction variableValuesFunction, List<Expression> expressions) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        double[] minMax = computeMinMax(valueTuples, variableValuesFunction, expressions);
        return minMax[MIN_INDEX];
    }
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
     * @return the result of the evaluation
     */
    private double evaluateExpression(Expression expression, VariableValuesFunction variableValuesFunction, List<Double> tuple) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        return evaluateNode(expression.getRoot(), variableValuesFunction, tuple);
    }
    private double evaluateNode(Node node, VariableValuesFunction variableValuesFunction, List<Double> tuple) throws DivisionByZeroException, ZeroOverZeroException, NotANumberException {
        if (node instanceof Constant) {
            return ((Constant) node).getValue();
        }
        else if (node instanceof Variable) {
            String variableName = ((Variable) node).getName();
            int variableIndex = variableValuesFunction.getVariableIndex(variableName);
            return tuple.get(variableIndex);
        }
        else {
            Operator operator = (Operator) node;
            List<Node> children = operator.getChildren();
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
            return operator.getType().getFunction().apply(childValues);
        }
    }
}
