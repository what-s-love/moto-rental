package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.CalendarDayDto;
import ge.tsepesh.motorental.dto.ShiftAvailabilityDto;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {
    //ToDo Проверить код
    //ToDo Убрать комментарии
    //ToDo Добавить необходимые DTO

    private final ShiftRepository shiftRepository;
    private final BikeAvailabilityService bikeAvailabilityService;

    @Transactional(readOnly = true)
    public List<CalendarDayDto> getCalendarForMonth(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<Shift> allShifts = shiftRepository.findAll();
        
        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> buildCalendarDay(date, allShifts))
                .collect(Collectors.toList());
    }

    private CalendarDayDto buildCalendarDay(LocalDate date, List<Shift> shifts) {
        List<ShiftAvailabilityDto> shiftAvailabilities = shifts.stream()
                .map(shift -> buildShiftAvailability(date, shift))
                .collect(Collectors.toList());
        
        return CalendarDayDto.builder()
                .date(date)
                .shifts(shiftAvailabilities)
                .build();
    }

    private ShiftAvailabilityDto buildShiftAvailability(LocalDate date, Shift shift) {
        long availableBikes = bikeAvailabilityService.getTotalAvailableBikesForDateAndShift(date, shift.getId());
        
        return ShiftAvailabilityDto.builder()
                .shiftId(shift.getId())
                .shiftName(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .availableBikes(availableBikes)
                .hasRoute(false) // Будет определяться позже при наличии заездов
                .build();
    }
}











