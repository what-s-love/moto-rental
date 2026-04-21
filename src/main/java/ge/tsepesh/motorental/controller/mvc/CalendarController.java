package ge.tsepesh.motorental.controller.mvc;

import ge.tsepesh.motorental.dto.CalendarDayDto;
import ge.tsepesh.motorental.dto.RideCalendarDto;
import ge.tsepesh.motorental.dto.ShiftDto;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.service.CalendarService;
import ge.tsepesh.motorental.service.ShiftService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;
    private final ShiftService shiftService;

    @GetMapping
    public String calendar(Model model) {
        // Получаем текущий месяц
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        // Получаем все заезды за текущий месяц
        List<RideCalendarDto> rides = calendarService.getRidesForDateRange(startDate, endDate);
        // Получаем доступные смены
        List<ShiftDto> shifts = shiftService.getEnabledShifts();

        log.info("Rides found: {}", rides.size());
        log.info("Shifts found: {}", shifts.size());

        model.addAttribute("rides", rides);
        model.addAttribute("shifts", shifts);
        model.addAttribute("currentMonth", currentMonth);
        return "calendar";
    }

    @GetMapping("/api/month/{year}/{month}")
    @ResponseBody
    public ResponseEntity<List<CalendarDayDto>> getCalendarData(
            @PathVariable int year,
            @PathVariable int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        List<CalendarDayDto> calendarDays = calendarService.getCalendarForMonth(yearMonth);
        return ResponseEntity.ok(calendarDays);
    }

    @GetMapping("/api/rides/{date}")
    @ResponseBody
    public ResponseEntity<List<RideCalendarDto>> getRidesForDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<RideCalendarDto> rides = calendarService.getRidesForDate(date);
        return ResponseEntity.ok(rides);
    }
}