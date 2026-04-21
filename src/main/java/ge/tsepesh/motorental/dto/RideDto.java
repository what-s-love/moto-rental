package ge.tsepesh.motorental.dto;

import ge.tsepesh.motorental.dto.route.RouteDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

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