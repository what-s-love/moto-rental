package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class RideDto {
    private Integer id;
    private LocalDate date;
    private ShiftDto shift;
    private RouteDto route;
    private Integer participantCount;
    private Integer maxParticipants;
    private Boolean hasAvailableSlots;
}