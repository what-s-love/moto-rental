package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class RideCalendarDto {
    private Integer rideId;
    private LocalDate date;
    private Integer shiftId;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer participantCount;
    private Integer routeId;
    private String routeName;
    private Long availableBikes;
    private Long totalBikes;
    private Boolean isFull;
}