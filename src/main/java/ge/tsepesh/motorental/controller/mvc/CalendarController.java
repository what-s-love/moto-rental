package ge.tsepesh.motorental.controller.mvc;

import ge.tsepesh.motorental.dto.CalendarDayDto;
import ge.tsepesh.motorental.dto.RideCalendarDto;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/calendar")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    public String calendar(Model model) {
        // Получаем текущий месяц
        YearMonth currentMonth = YearMonth.now();
        LocalDate startDate = currentMonth.atDay(1);
        LocalDate endDate = currentMonth.atEndOfMonth();

        // Получаем все заезды за текущий месяц
        List<RideCalendarDto> rides = calendarService.getRidesForDateRange(startDate, endDate);

        model.addAttribute("rides", rides);
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