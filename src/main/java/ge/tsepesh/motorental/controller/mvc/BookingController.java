package ge.tsepesh.motorental.controller.mvc;

import ge.tsepesh.motorental.dto.BikeAvailabilityDto;
import ge.tsepesh.motorental.dto.booking.BookingRequestDto;
import ge.tsepesh.motorental.dto.booking.BookingResponseDto;
import ge.tsepesh.motorental.dto.RideDto;
import ge.tsepesh.motorental.dto.route.RouteDto;
import ge.tsepesh.motorental.dto.ShiftDto;
import ge.tsepesh.motorental.exception.ResourceNotFoundException;
import ge.tsepesh.motorental.model.Banner;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.model.Ride;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.BannerRepository;
import ge.tsepesh.motorental.repository.BookingRepository;
import ge.tsepesh.motorental.repository.RideRepository;
import ge.tsepesh.motorental.repository.ShiftRepository;
import ge.tsepesh.motorental.service.BikeAvailabilityService;
import ge.tsepesh.motorental.service.BookingService;
import ge.tsepesh.motorental.service.RouteService;
import ge.tsepesh.motorental.util.DateUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingRepository bookingRepository;

    private final RideRepository rideRepository;
    private final ShiftRepository shiftRepository;
    private final BannerRepository bannerRepository;
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
        if (date.isBefore(LocalDate.now()) || date.isEqual(LocalDate.now())) {
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
        model.addAttribute("isWeekend", DateUtil.isWeekend(date));
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

        return "booking";
    }

    @PostMapping(value = "/booking", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> createBookingJson(@Valid @RequestBody BookingRequestDto request) {
        String sessionId = UUID.randomUUID().toString();
        BookingResponseDto response = bookingService.createBooking(request, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/confirmation/{bookingId}")
    public String showBookingConfirmation(@PathVariable Integer bookingId, Model model) {
        log.info("Showing booking confirmation page for booking ID: {}", bookingId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            throw new ResourceNotFoundException("error.booking.not.found");
        }
        model.addAttribute("bookingId", booking.get().getId());
        return "confirmation";
    }

    @GetMapping("/ride/special")
    public String showSpecialRideBookingPage(
            @RequestParam Integer bannerId,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Получить баннер
        Banner banner = bannerRepository.findById(bannerId)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found"));

        // Проверить, что баннер активен
        if (!banner.getEnabled()) {
            redirectAttributes.addFlashAttribute("error",
                    "Специальное предложение больше не доступно");
            return "redirect:/";
        }

        // Проверить, что маршрут специальный
        Route route = banner.getRoute();
        if (!route.getIsSpecial() || !route.getEnabled()) {
            redirectAttributes.addFlashAttribute("error",
                    "Маршрут не доступен");
            return "redirect:/";
        }

        // Проверить свободные места
        long availableBikes = bikeAvailabilityService
                .getTotalAvailableBikesForDateAndShift(
                        banner.getRideDate(),
                        banner.getShift().getId()
                );

        if (availableBikes <= 0) {
            redirectAttributes.addFlashAttribute("error",
                    "Нет свободных мест на этот заезд");
            return "redirect:/";
        }

        // Получить существующий заезд или null
        Optional<Ride> existingRide = rideRepository
                .findByDateAndShift(banner.getRideDate(), banner.getShift().getId());

        // Получить доступные мотоциклы
        List<BikeAvailabilityDto> availableBikesList =
                bikeAvailabilityService.getAvailableBikesForDateAndShift(
                        banner.getRideDate(),
                        banner.getShift().getId()
                );

        // Добавить данные в модель
        model.addAttribute("date", banner.getRideDate());
        model.addAttribute("shiftId", banner.getShift().getId());
        model.addAttribute("shift", mapToShiftDto(banner.getShift()));
        model.addAttribute("route", routeService.convertToDto(route));
        model.addAttribute("availableBikes", availableBikesList);
        model.addAttribute("maxParticipants", (int) availableBikes);
        model.addAttribute("isSpecialBooking", true);  // Флаг специального бронирования
        model.addAttribute("routeLocked", true);  // Маршрут нельзя менять

        if (existingRide.isPresent()) {
            model.addAttribute("existingRide", mapToRideDto(existingRide.get()));
        }

        return "booking-special";
    }

    private ShiftDto mapToShiftDto(Shift shift) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return ShiftDto.builder()
                .id(shift.getId())
                .name(shift.getName())
                .startTime(shift.getStartTime().format(formatter))
                .endTime(shift.getEndTime().format(formatter))
                .build();
    }

    private RideDto mapToRideDto(Ride ride) {
        int participantCount = ride.getParticipants() != null ? ride.getParticipants().size() : 0;
        long totalBikes = bikeAvailabilityService.getTotalEnabledBikes();
        
        return RideDto.builder()
                .id(ride.getId())
                .date(ride.getDate())
                .shift(mapToShiftDto(ride.getShift()))
                .route(routeService.convertToDto(ride.getRoute()))
                .participantCount(participantCount)
                .maxParticipants((int) totalBikes)
                .hasAvailableSlots(participantCount < totalBikes)
                .build();
    }
}


