package co.edu.cesde.inventarionoche.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return crearRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // 400
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 400
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientStock(InsufficientStockException ex) {
        return crearRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // 409
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return crearRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // 400 - errores de @Valid en los controladores
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBeanValidation(MethodArgumentNotValidException ex) {
        StringBuilder mensaje = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                mensaje.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; "));
        return crearRespuesta(HttpStatus.BAD_REQUEST, mensaje.toString().trim());
    }

    private ResponseEntity<Map<String, Object>> crearRespuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return ResponseEntity.status(status).body(body);
    }
}
