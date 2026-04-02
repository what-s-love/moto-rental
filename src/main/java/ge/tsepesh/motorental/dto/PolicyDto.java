package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PolicyDto {
    private Integer id;
    private String text;
    private String version;
    private LocalDateTime createdAt;
    private Boolean isActive;
}
