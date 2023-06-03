package it.units.expressionserver.exceptions;

public class DivisionByZeroException extends ComputationException{
    public DivisionByZeroException(String message) {
        super(message);
    }
}
