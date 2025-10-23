package ge.tsepesh.motorental.exception.handler;

import ge.tsepesh.motorental.exception.BookingException;
import ge.tsepesh.motorental.exception.ResourceNotFoundException;
import ge.tsepesh.motorental.exception.ValidationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @ExceptionHandler(BookingException.class)
    public String handleBookingException(BookingException ex, WebRequest request, Locale locale) {
        log.error("Booking exception occurred: {}", ex.getMessage(), ex);

        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);

        Map<String, Object> model = new HashMap<>();
        model.put("status", HttpStatus.BAD_REQUEST.value());
        model.put("error", "Booking Error");
        model.put("message", message);
        model.put("timestamp", LocalDateTime.now());

        request.setAttribute("errorModel", model, WebRequest.SCOPE_REQUEST);
        return "error";
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request, Locale locale) {
        log.error("Resource not found: {}", ex.getMessage(), ex);

        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);

        Map<String, Object> model = new HashMap<>();
        model.put("status", HttpStatus.NOT_FOUND.value());
        model.put("error", "Not Found");
        model.put("message", message);
        model.put("timestamp", LocalDateTime.now());

        request.setAttribute("errorModel", model, WebRequest.SCOPE_REQUEST);
        return "error";
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException ex, WebRequest request, Locale locale) {
        log.error("Validation exception: {}", ex.getMessage(), ex);

        String message = messageSource.getMessage(ex.getMessageKey(), ex.getMessageArgs(), locale);

        Map<String, Object> model = new HashMap<>();
        model.put("status", HttpStatus.BAD_REQUEST.value());
        model.put("error", "Validation Error");
        model.put("message", message);
        model.put("timestamp", LocalDateTime.now());

        request.setAttribute("errorModel", model, WebRequest.SCOPE_REQUEST);
        return "error";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request, Locale locale) {
        log.error("Entity not found: {}", ex.getMessage(), ex);

        String message = messageSource.getMessage("error.not.found", null, locale);

        Map<String, Object> model = new HashMap<>();
        model.put("status", HttpStatus.NOT_FOUND.value());
        model.put("error", "Not Found");
        model.put("message", message);
        model.put("timestamp", LocalDateTime.now());

        request.setAttribute("errorModel", model, WebRequest.SCOPE_REQUEST);
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request, Locale locale) {
        log.error("Illegal argument: {}", ex.getMessage(), ex);

        String message = messageSource.getMessage("error.bad.request", null, locale);

        Map<String, Object> model = new HashMap<>();
        model.put("status", HttpStatus.BAD_REQUEST.value());
        model.put("error", "Bad Request");
        model.put("message", message);
        model.put("timestamp", LocalDateTime.now());

        request.setAttribute("errorModel", model, WebRequest.SCOPE_REQUEST);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, WebRequest request, Locale locale) {
        log.error("Unexpected error occurred", ex);

        String message = messageSource.getMessage("error.internal.server", null, locale);

        Map<String, Object> model = new HashMap<>();
        model.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.put("error", "Internal Server Error");
        model.put("message", message);
        model.put("timestamp", LocalDateTime.now());

        request.setAttribute("errorModel", model, WebRequest.SCOPE_REQUEST);
        return "error";
    }
}