package com.project.domain.variablevaluesfunction;

import com.project.exceptions.VariableValuesFunctionException;
import com.project.exceptions.VariableValuesFunctionParsingException;
import com.project.service.Computer;

import java.util.ArrayList;
import java.util.List;

public class VariableValuesFunctionParser {
    public VariableValuesFunction parse(String variableValuesFunctionString, VariableValuesFunction.ValuesKind valuesKind, Computer.ComputationKind computationKind) throws VariableValuesFunctionException {
        // Split the variable-values function part by ","
        String[] variableValuesStrings = variableValuesFunctionString.split(",");
        List<VariableValues> variableValuesList = new ArrayList<>();

        for (String variableValuesString : variableValuesStrings) {
            String[] valuesParts = variableValuesString.split(":");
            if (valuesParts.length != 4) {
                throw new VariableValuesFunctionParsingException("Invalid variable values function format. " +
                        "Required format for variable values: VarName\":\"JavaNum\":\"JavaNum\":\"JavaNum");
            }

            String variable = valuesParts[0];
            double lower = Double.parseDouble(valuesParts[1]);
            double step = Double.parseDouble(valuesParts[2]);
            double upper = Double.parseDouble(valuesParts[3]);
            VariableValues variableValues;
            try {
                variableValues = new VariableValues(variable, lower, step, upper);
            } catch (VariableValuesFunctionException e) {
                throw new VariableValuesFunctionParsingException("Invalid variable values \" " + variableValuesString + " \" : " + e.getMessage());
            }
            variableValuesList.add(variableValues);
        }

        return new VariableValuesFunction(variableValuesList, valuesKind, computationKind);
    }
}
