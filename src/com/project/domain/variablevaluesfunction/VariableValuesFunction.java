package com.project.domain.variablevaluesfunction;
import com.project.exceptions.VariableValuesFunctionBuildingException;
import com.project.exceptions.VariableValuesFunctionException;
import com.project.service.Computer;
import java.util.*;


public class VariableValuesFunction {
    private final static int PRECISION = 12;
    public enum ValuesKind {

        GRID("GRID"),
        LIST("LIST");

        private final String valuesKindString;

        private static final Map<String, ValuesKind> stringToEnumMap = new HashMap<>();

        static {
            for (ValuesKind valuesKind : ValuesKind.values()) {
                stringToEnumMap.put(valuesKind.getRequestString(), valuesKind);
            }
        }

        ValuesKind(String valuesKindString) {
            this.valuesKindString = valuesKindString;
        }

        public String getRequestString() {
            return valuesKindString;
        }

        public static ValuesKind fromRequestString(String valuesKindString) {
            return stringToEnumMap.get(valuesKindString);
        }
    }
    private final Map<String, List<Double>> functionMap;
    private final ValuesKind valuesKind;
    private final Computer.ComputationKind computationKind;

    /**
     * Constructs a new VariableValuesFunction instance.
     *
     * @param variableValuesList list of VariableValues objects specifying the variable names and their ranges
     * @param valuesKind the kind of values to generate (GRID or LIST)
     * @param computationKind the kind of computation to perform (MIN, MAX, AVG, or COUNT)
     */
    public VariableValuesFunction(List<VariableValues> variableValuesList, ValuesKind valuesKind, Computer.ComputationKind computationKind) {
        this.functionMap = new HashMap<>();
        this.valuesKind = valuesKind;
        this.computationKind = computationKind;
        for (VariableValues variableValues : variableValuesList) {
            String variable = variableValues.getVariable();
            double lower = round(variableValues.getLower());
            double step = round(variableValues.getStep());
            double upper = round(variableValues.getUpper());
            if (upper < lower) {
                // Empty set
                continue;
            }
            List<Double> values = new ArrayList<>();
            for (double value = lower; value <= upper; value = round(value + step)) {
                values.add(value);
            }
            functionMap.put(variable, values);
        }
    }
    private double round(double value) {

        long factor = (long) Math.pow(10, VariableValuesFunction.PRECISION);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
    public Computer.ComputationKind getComputationKind() {
        return this.computationKind;
    }
    /**
     * Generates a list of tuples of variable values based on the specified values kind.
     *
     * @return a list of tuples of variable values
     * @throws VariableValuesFunctionException if an invalid values kind is specified
     */
    public List<List<Double>> generateValueTuples() throws VariableValuesFunctionException {
        switch (valuesKind) {
            case GRID:
                return generateGridValueTuples();
            case LIST:
                return generateListValueTuples();
            default:
                throw new VariableValuesFunctionException("Invalid values kind: " + valuesKind.valuesKindString);
        }
    }

    /**
     * Generates a list of tuples of variable values for a grid of variables.
     *
     * @return a list of tuples of variable values
     */
    private List<List<Double>> generateGridValueTuples() {
        List<List<Double>> valueLists = new ArrayList<>(functionMap.values());
        return cartesianProduct(0, valueLists);
    }

    /**
     * Gets the index of a variable in the variable list.
     *
     * @param variableName the name of the variable
     * @return the index of the variable in the variable list
     * @throws VariableValuesFunctionException if the variable is not found
     */
    public int getVariableIndex(String variableName) throws VariableValuesFunctionException{
        List<String> variables = new ArrayList<>(functionMap.keySet());
        int variableIndex = variables.indexOf(variableName);
        if (variableIndex == -1) {
            throw new VariableValuesFunctionException("Invalid variable '" + variableName + "'");
        }
        return variableIndex;
    }
    private List<List<Double>> cartesianProduct(int index, List<List<Double>> lists) {
        List<List<Double>> result = new ArrayList<>();
        if (index == lists.size()) {
            result.add(new ArrayList<>());
        } else {
            for (Double value : lists.get(index)) {
                for (List<Double> list : cartesianProduct(index + 1, lists)) {
                    list.add(0, value);
                    result.add(list);
                }
            }
        }
        return result;
    }
    private List<List<Double>> generateListValueTuples() throws VariableValuesFunctionException {
        List<List<Double>> valueLists = new ArrayList<>(functionMap.values());
        int size = valueLists.get(0).size();
        for (List<Double> list : valueLists) {
            if (list.size() != size) {
                throw new VariableValuesFunctionBuildingException("All variable value lists must have the same length for LIST values kind.");
            }
        }

        List<List<Double>> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            List<Double> tuple = new ArrayList<>();
            for (List<Double> list : valueLists) {
                tuple.add(list.get(i));
            }
            result.add(tuple);
        }
        return result;
    }
}
