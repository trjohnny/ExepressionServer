package it.units.expressionserver.domain.variablevaluesfunction;

import it.units.expressionserver.exceptions.VariableValuesFunctionException;

public class VariableValues {
    private final String variable;
    private final double lower;
    private final double step;
    private final double upper;

    public VariableValues(String variable, double lower, double step, double upper) throws VariableValuesFunctionException {
        if (step <= 0) {
            throw new VariableValuesFunctionException("Step must be greater than 0.");
        }

        this.variable = variable;
        this.lower = lower;
        this.step = step;
        this.upper = upper;
    }
    public String getVariable() {
        return variable;
    }

    public double getLower() {
        return lower;
    }

    public double getStep() {
        return step;
    }

    public double getUpper() {
        return upper;
    }
}
