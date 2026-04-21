package ge.tsepesh.motorental.dto.policy;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PolicyAdminDto {
    private Integer id;
    private String version;
    private LocalDateTime createdAt;
    private Boolean isActive;
}