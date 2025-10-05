package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ErrorResponseDto {
    private String message;
    private String error;
    private Integer status;
    private String path;
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}