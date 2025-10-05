package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class ShiftAvailabilityDto {
    private Integer shiftId;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long availableBikes;
    private Boolean hasRoute;
    private Boolean isAvailable;
    private String unavailableReason;
}