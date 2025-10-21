package ge.tsepesh.motorental.controller.mvc;

import ge.tsepesh.motorental.dto.BikeAvailabilityDto;
import ge.tsepesh.motorental.dto.BookingRequestDto;
import ge.tsepesh.motorental.dto.BookingResponseDto;
import ge.tsepesh.motorental.dto.RideDto;
import ge.tsepesh.motorental.dto.RouteDto;
import ge.tsepesh.motorental.dto.ShiftDto;
import ge.tsepesh.motorental.model.Ride;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.RideRepository;
import ge.tsepesh.motorental.repository.RouteRepository;
import ge.tsepesh.motorental.repository.ShiftRepository;
import ge.tsepesh.motorental.service.BikeAvailabilityService;
import ge.tsepesh.motorental.service.BookingService;
import ge.tsepesh.motorental.service.RouteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final RideRepository rideRepository;
    private final ShiftRepository shiftRepository;
    private final RouteRepository routeRepository;
    private final BikeAvailabilityService bikeAvailabilityService;
    private final BookingService bookingService;
    private final RouteService routeService;

    @GetMapping("/ride")
    public String showRideBookingPage(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer shiftId,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Showing ride booking page for date: {} and shift: {}", date, shiftId);

        // Валидация входных параметров
        if (date.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("error", "Нельзя бронировать заезды в прошлом");
            return "redirect:/calendar";
        }

        // Проверка существования смены
        Optional<Shift> shiftOpt = shiftRepository.findById(shiftId);
        if (shiftOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Смена не найдена");
            return "redirect:/calendar";
        }

        Shift shift = shiftOpt.get();

        // Проверка наличия свободных мест
        long availableBikes = bikeAvailabilityService.getTotalAvailableBikesForDateAndShift(date, shiftId);
        if (availableBikes <= 0) {
            redirectAttributes.addFlashAttribute("error", "Нет свободных мест на выбранную дату и смену");
            return "redirect:/calendar";
        }

        // Поиск существующего заезда
        Optional<Ride> existingRideOpt = rideRepository.findByDateAndShift(date, shiftId);

        // Получение всех мотоциклов с информацией о занятости
        List<BikeAvailabilityDto> availableBikesList = bikeAvailabilityService.getAvailableBikesForDateAndShift(date, shiftId);

        // Добавление данных в модель
        model.addAttribute("date", date);
        model.addAttribute("shiftId", shiftId);
        model.addAttribute("shift", mapToShiftDto(shift));
        model.addAttribute("availableBikes", availableBikesList);
        model.addAttribute("maxParticipants", (int) availableBikes);

        if (existingRideOpt.isPresent()) {
            // Заезд уже создан
            Ride existingRide = existingRideOpt.get();
            model.addAttribute("existingRide", mapToRideDto(existingRide));
            log.info("Found existing ride: {} with {} participants", existingRide.getId(), 
                    existingRide.getParticipants() != null ? existingRide.getParticipants().size() : 0);
        } else {
            // Заезд не создан - показываем маршруты для выбора
            List<RouteDto> routes = routeService.getAllActiveRoutes();
            model.addAttribute("routes", routes);
            log.info("No existing ride found, showing {} routes for selection", routes.size());
        }

        return "ride";
    }

    @PostMapping("/booking")
    public String createBooking(
            @Valid @RequestBody BookingRequestDto request,
            BindingResult bindingResult,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {

        log.info("Creating booking for date: {}, shift: {}, route: {} with {} participants",
                request.getDate(), request.getShiftId(), request.getRouteId(), request.getParticipants().size());

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors in booking request: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации данных");
            return "redirect:/ride?date=" + request.getDate() + "&shiftId=" + request.getShiftId();
        }

        try {
            // Генерируем sessionId для резервации мотоциклов
            String sessionId = UUID.randomUUID().toString();
            
            BookingResponseDto response = bookingService.createBooking(request, sessionId);
            
            log.info("Booking created successfully: {}", response.getBookingId());
            redirectAttributes.addFlashAttribute("success", "Бронирование создано успешно");
            
            return "redirect:/booking/confirmation/" + response.getBookingId();
            
        } catch (Exception e) {
            log.error("Error creating booking", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании бронирования: " + e.getMessage());
            return "redirect:/ride?date=" + request.getDate() + "&shiftId=" + request.getShiftId();
        }
    }

    private ShiftDto mapToShiftDto(Shift shift) {
        return ShiftDto.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .build();
    }

    private RideDto mapToRideDto(Ride ride) {
        int participantCount = ride.getParticipants() != null ? ride.getParticipants().size() : 0;
        long totalBikes = bikeAvailabilityService.getTotalEnabledBikes();
        
        return RideDto.builder()
                .id(ride.getId())
                .date(ride.getDate())
                .shift(mapToShiftDto(ride.getShift()))
                .route(mapToRouteDto(ride.getRoute()))
                .participantCount(participantCount)
                .maxParticipants((int) totalBikes)
                .hasAvailableSlots(participantCount < totalBikes)
                .build();
    }

    private RouteDto mapToRouteDto(Route route) {
        return RouteDto.builder()
                .id(route.getId())
                .distance(route.getDistance())
                .difficulty(ge.tsepesh.motorental.enums.Difficulty.values()[route.getDifficulty()])
                .difficultyDisplayName(ge.tsepesh.motorental.enums.Difficulty.values()[route.getDifficulty()].getDisplayName())
                .price(route.getPrice())
                .mapPath(route.getMapPath())
                .description(route.getDescription())
                .estimatedDuration(route.getDuration())
                .isAvailableForBeginners(route.getDifficulty() <= 1)
                .build();
    }
}
