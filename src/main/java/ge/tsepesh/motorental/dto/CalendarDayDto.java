package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CalendarDayDto {
    private LocalDate date;
    private List<ShiftAvailabilityDto> shifts;
    private boolean isPast;
    private boolean isToday;
    private boolean isWeekend;
}