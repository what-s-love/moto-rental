package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.CalendarDayDto;
import ge.tsepesh.motorental.dto.RideCalendarDto;
import ge.tsepesh.motorental.dto.ShiftAvailabilityDto;
import ge.tsepesh.motorental.model.Ride;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.RideRepository;
import ge.tsepesh.motorental.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final ShiftRepository shiftRepository;
    private final RideRepository rideRepository;
    private final BikeAvailabilityService bikeAvailabilityService;

    @Transactional(readOnly = true)
    public List<CalendarDayDto> getCalendarForMonth(YearMonth yearMonth) {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Shift> allShifts = shiftRepository.findAll();
        List<Ride> ridesInMonth = rideRepository.findRidesByDateRange(startDate, endDate);

        // Группируем заезды по датам
        Map<LocalDate, List<Ride>> ridesByDate = ridesInMonth.stream()
                .collect(Collectors.groupingBy(Ride::getDate));

        return startDate.datesUntil(endDate.plusDays(1))
                .map(date -> buildCalendarDay(date, allShifts, ridesByDate.get(date)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RideCalendarDto> getRidesForDate(LocalDate date) {
        List<Ride> rides = rideRepository.findRidesByDateRange(date, date);

        return rides.stream()
                .map(this::mapToRideCalendarDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RideCalendarDto> getRidesForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Ride> rides = rideRepository.findRidesByDateRange(startDate, endDate);

        return rides.stream()
                .map(this::mapToRideCalendarDto)
                .collect(Collectors.toList());
    }

    private CalendarDayDto buildCalendarDay(LocalDate date, List<Shift> shifts, List<Ride> ridesForDate) {
        List<ShiftAvailabilityDto> shiftAvailabilities = shifts.stream()
                .map(shift -> buildShiftAvailability(date, shift, ridesForDate))
                .collect(Collectors.toList());

        return CalendarDayDto.builder()
                .date(date)
                .shifts(shiftAvailabilities)
                .isPast(date.isBefore(LocalDate.now()))
                .isToday(date.equals(LocalDate.now()))
                .isWeekend(date.getDayOfWeek().getValue() >= 6)
                .build();
    }

    private ShiftAvailabilityDto buildShiftAvailability(LocalDate date, Shift shift, List<Ride> ridesForDate) {
        long availableBikes = bikeAvailabilityService.getTotalAvailableBikesForDateAndShift(date, shift.getId());

        // Проверяем, есть ли заезд для этой смены
        boolean hasRoute = ridesForDate != null && ridesForDate.stream()
                .anyMatch(ride -> ride.getShift().getId().equals(shift.getId()));

        return ShiftAvailabilityDto.builder()
                .shiftId(shift.getId())
                .shiftName(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .availableBikes(availableBikes)
                .hasRoute(hasRoute)
                .isAvailable(availableBikes > 0)
                .build();
    }

    private RideCalendarDto mapToRideCalendarDto(Ride ride) {
        long participantCount = ride.getParticipants() != null ? ride.getParticipants().size() : 0;
        long availableBikes = bikeAvailabilityService.getTotalAvailableBikesForDateAndShift(
                ride.getDate(), ride.getShift().getId());
        long totalEnabledBikes = bikeAvailabilityService.getTotalEnabledBikes();

        return RideCalendarDto.builder()
                .rideId(ride.getId())
                .date(ride.getDate())
                .shiftId(ride.getShift().getId())
                .shiftName(ride.getShift().getName())
                .startTime(ride.getShift().getStartTime())
                .endTime(ride.getShift().getEndTime())
                .participantCount((int) participantCount)
                .routeId(ride.getRoute().getId())
                .routeName(ride.getRoute().getName())
                .routeDescription(ride.getRoute().getDescription())
                .availableBikes(availableBikes)
                .totalBikes(totalEnabledBikes)
                .isFull(participantCount >= totalEnabledBikes)
                .build();
    }
}


