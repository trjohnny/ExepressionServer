package it.units.expressionserver.domain.variablevaluesfunction;

import it.units.expressionserver.exceptions.VariableValuesFunctionException;
import it.units.expressionserver.exceptions.VariableValuesFunctionParsingException;
import it.units.expressionserver.service.Computer;

import java.util.ArrayList;
import java.util.List;

public class VariableValuesFunctionParser {
    /**
     * Parses a string representation of a VariableValuesFunction and returns the corresponding object.
     * The string should have the following format: "VarName:JavaNum:JavaNum:JavaNum",
     * where "VarName" is the name of the variable, and the following numbers represent
     * the lower limit, step size, and upper limit, respectively, of that variable.
     *
     * @param variableValuesFunctionString The string to parse.
     * @param valuesKind The kind of values being parsed.
     * @param computationKind The kind of computation to be performed on these variable values.
     * @return The parsed VariableValuesFunction object.
     * @throws VariableValuesFunctionException If an error occurs while parsing the variable values function string.
     */
    public VariableValuesFunction parse(String variableValuesFunctionString, VariableValuesFunction.ValuesKind valuesKind, Computer.ComputationKind computationKind) throws VariableValuesFunctionException {

        String[] variableValuesStrings = variableValuesFunctionString.split(",");
        List<VariableValues> variableValuesList = new ArrayList<>();

        for (String variableValuesString : variableValuesStrings) {
            String[] valuesParts = variableValuesString.split(":");
            if (valuesParts.length != 4) {
                throw new VariableValuesFunctionParsingException("Invalid variable values function format. " +
                        "Required format for variable values: VarName:JavaNum:JavaNum:JavaNum");
            }

            String variable = valuesParts[0];
            double lower = Double.parseDouble(valuesParts[1]);
            double step = Double.parseDouble(valuesParts[2]);
            double upper = Double.parseDouble(valuesParts[3]);
            VariableValues variableValues;
            try {
                variableValues = new VariableValues(variable, lower, step, upper);
            } catch (VariableValuesFunctionException e) {
                String errorMessage = String.format("Invalid variable values '%1$s' : %2$s", variableValuesString, e.getMessage());
                throw new VariableValuesFunctionParsingException(errorMessage);
            }
            variableValuesList.add(variableValues);
        }

        return new VariableValuesFunction(variableValuesList, valuesKind, computationKind);
    }
}
