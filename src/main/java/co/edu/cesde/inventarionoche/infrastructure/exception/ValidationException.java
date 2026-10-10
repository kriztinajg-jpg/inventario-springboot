package co.edu.cesde.inventarionoche.infrastructure.exception;

public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
