package it.units.expressionserver.protocol.response;
/**
 * The ErrorResponse class implements the Response interface and represents an error response from the server.
 * It includes an error message detailing the cause of the error.
 */
public class ErrorResponse implements Response {

    private final String errorMessage;

    /**
     * Constructs a new ErrorResponse with the specified error message.
     *
     * @param errorMessage The error message detailing the cause of the error.
     */
    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Returns a string representation of the ErrorResponse.
     * The format is "ERR;errorMessage"
     *
     * @return A string representation of the ErrorResponse.
     */
    @Override
    public String toString() {
        return "ERR;" + errorMessage;
    }
}
