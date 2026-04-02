package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ConsentDto {
    private Integer id;
    private Integer clientId;
    private Integer policyId;
    private Integer bookingId;
    // SHA-256 от clientId + ":" + policyId + ":" + version + ":" + createdAt
    private String hash;
    private LocalDateTime createdAt;

}
