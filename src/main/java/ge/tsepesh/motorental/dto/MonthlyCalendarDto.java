package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.YearMonth;
import java.util.List;

@Data
@Builder
public class MonthlyCalendarDto {
    private YearMonth yearMonth;
    private String monthDisplayName;
    private List<CalendarDayDto> days;
    private Integer totalAvailableDays;
    private Integer totalUnavailableDays;
}