package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AvailabilityCheckDto {
    private LocalDate date;
    private Integer shiftId;
    private Integer totalBikes;
    private Integer availableBikes;
    private Integer occupiedBikes;
    private List<BikeAvailabilityDto> availableBikesList;
    private Boolean hasAvailableSlots;
    private String unavailableReason;
}