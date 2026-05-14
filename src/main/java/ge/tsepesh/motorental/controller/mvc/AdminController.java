package ge.tsepesh.motorental.controller.mvc;

import ge.tsepesh.motorental.dto.BannerCreateDto;
import ge.tsepesh.motorental.dto.BannerDto;
import ge.tsepesh.motorental.dto.BannerUpdateDto;
import ge.tsepesh.motorental.dto.bike.BikeAvailabilityDto;
import ge.tsepesh.motorental.dto.bike.BikeCreateDto;
import ge.tsepesh.motorental.dto.bike.BikeUpdateDto;
import ge.tsepesh.motorental.dto.DashboardStatsDto;
import ge.tsepesh.motorental.dto.LimitUpdateDto;
import ge.tsepesh.motorental.dto.LimitCreateDto;
import ge.tsepesh.motorental.dto.ShiftUpdateDto;
import ge.tsepesh.motorental.dto.ShiftCreateDto;
import ge.tsepesh.motorental.dto.booking.BookingAdminDto;
import ge.tsepesh.motorental.dto.booking.BookingCreateAdminDto;
import ge.tsepesh.motorental.dto.policy.PolicyAdminDto;
import ge.tsepesh.motorental.dto.policy.PolicyCreateDto;
import ge.tsepesh.motorental.dto.policy.PolicyDto;
import ge.tsepesh.motorental.dto.route.RouteCreateDto;
import ge.tsepesh.motorental.dto.route.RouteDto;
import ge.tsepesh.motorental.dto.route.RouteUpdateDto;
import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.enums.TransmissionType;
import ge.tsepesh.motorental.model.Banner;
import ge.tsepesh.motorental.model.Bike;
import ge.tsepesh.motorental.model.Limit;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.service.BannerService;
import ge.tsepesh.motorental.service.BikeAvailabilityService;
import ge.tsepesh.motorental.service.BikeService;
import ge.tsepesh.motorental.service.BookingService;
import ge.tsepesh.motorental.service.LimitService;
import ge.tsepesh.motorental.service.PolicyService;
import ge.tsepesh.motorental.service.RouteService;
import ge.tsepesh.motorental.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final BookingService bookingService;
    private final ShiftService shiftService;
    private final LimitService limitService;
    private final PolicyService policyService;
    private final RouteService routeService;
    private final BikeService bikeService;
    private final BannerService bannerService;
    private final BikeAvailabilityService bikeAvailabilityService;

    // ==================== LOGIN ====================

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        return "admin/login";
    }

    // ==================== DASHBOARD ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        DashboardStatsDto stats = bookingService.getDashboardStats();

        model.addAttribute("totalBookings", stats.getTotalBookings());
        model.addAttribute("paidCount", stats.getPaidCount());
        model.addAttribute("expiredCount", stats.getExpiredCount());
        model.addAttribute("completedCount", stats.getCompletedCount());

        model.addAttribute("weekTotalBookings", stats.getWeekTotalBookings());
        model.addAttribute("weekPendingPaymentCount", stats.getWeekPendingPaymentCount());
        model.addAttribute("weekPaidCount", stats.getWeekPaidCount());
        model.addAttribute("weekExpiredCount", stats.getWeekExpiredCount());

        return "admin/dashboard";
    }

    // ==================== SHIFTS ====================

    @GetMapping("/shifts")
    public String shifts(Model model) {
        List<Shift> shifts = shiftService.getAllShifts();
        model.addAttribute("shifts", shifts);
        return "admin/shifts/list";
    }

    @PostMapping("/shifts")
    public String updateShifts(@ModelAttribute ShiftUpdateDto.ShiftsUpdateForm shiftsUpdates,
                               RedirectAttributes redirectAttributes) {
        try {
            shiftService.updateShifts(shiftsUpdates.getShifts());
            redirectAttributes.addFlashAttribute("success", "Смены успешно обновлены");
        } catch (Exception e) {
            log.error("Error updating shifts", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении смен: " + e.getMessage());
        }
        return "redirect:/admin/shifts";
    }

    @GetMapping("/shifts/create")
    public String createShiftForm() {
        return "admin/shifts/create";
    }

    @PostMapping("/shifts/create")
    public String createShift(@Valid @ModelAttribute ShiftCreateDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/shifts/create";
        }

        try {
            Shift created = shiftService.createShift(dto);
            redirectAttributes.addFlashAttribute("success", "Смена \"" + created.getName() + "\" создана");
        } catch (Exception e) {
            log.error("Error creating shift", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/shifts/create";
        }

        return "redirect:/admin/shifts";
    }

    // ==================== LIMITS ====================

    @GetMapping("/limits")
    public String limits(Model model) {
        List<Limit> limits = limitService.getAllLimits();
        model.addAttribute("limits", limits);
        return "admin/limits/list";
    }
    @PostMapping("/limits")
    public String updateLimits(@ModelAttribute LimitUpdateDto.LimitsUpdateForm limitsForm,
                               RedirectAttributes redirectAttributes) {
        try {
            limitService.updateLimits(limitsForm.getLimits());
            redirectAttributes.addFlashAttribute("success", "Ограничения успешно обновлены");
        } catch (Exception e) {
            log.error("Error updating limits", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении ограничений: " + e.getMessage());
        }
        return "redirect:/admin/limits";
    }

    @GetMapping("/limits/create")
    public String createLimitForm() {
        return "admin/limits/create";
    }

    @PostMapping("/limits/create")
    public String createLimit(@Valid @ModelAttribute LimitCreateDto dto,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/limits/create";
        }

        // Валидация: heightMax должен быть больше heightMin
        if (dto.getHeightMax() <= dto.getHeightMin()) {
            redirectAttributes.addFlashAttribute("error", "Максимальный рост должен быть больше минимального");
            return "redirect:/admin/limits/create";
        }

        try {
            Limit created = limitService.createLimit(dto);
            redirectAttributes.addFlashAttribute("success",
                    "Ограничение создано (ID: " + created.getId() + ")");
        } catch (Exception e) {
            log.error("Error creating limit", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/limits/create";
        }

        return "redirect:/admin/limits";
    }

    // ==================== BOOKINGS ====================

    @GetMapping("/bookings")
    public String bookingsList(Model model) {
        List<BookingAdminDto> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin/booking/list";
    }

    @GetMapping("/bookings/create")
    public String createBookingForm(Model model) {
        // Даты: с завтрашнего дня + 30 дней
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<LocalDate> dates = tomorrow.datesUntil(tomorrow.plusDays(30))
                .collect(Collectors.toList());

        List<Shift> shifts = shiftService.getAllShifts();
        List<RouteDto> routes = routeService.getAllActiveRoutes();

        // Для упрощения — все активные байки (фильтрация на фронте опциональна)
        List<BikeAvailabilityDto> availableBikes = bikeService.getAllActiveBikes()
                .stream()
                .map(bike -> BikeAvailabilityDto.builder()
                        .id(bike.getId())
                        .brand(bike.getBrand())
                        .model(bike.getModel())
                        .engineCc(bike.getEngineCc())
                        .photoPath(bike.getPhotoPath())
                        .build())
                .toList();

        model.addAttribute("dates", dates);
        model.addAttribute("shifts", shifts);
        model.addAttribute("routes", routes);
        model.addAttribute("availableBikes", availableBikes);
        model.addAttribute("maxParticipants", availableBikes.size());

        return "admin/booking/create";
    }

    @PostMapping("/bookings")
    public String createBooking(@ModelAttribute BookingCreateAdminDto dto,
                                RedirectAttributes redirectAttributes) {
        try {
            BookingAdminDto created = bookingService.createBookingByAdmin(dto);
            redirectAttributes.addFlashAttribute("success", "Бронирование #" + created.getId() + " создано");
            return "redirect:/admin/bookings";
        } catch (Exception e) {
            log.error("Error creating booking by admin", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/bookings/create";
        }
    }

    @GetMapping("/bookings/{id}/edit")
    public String editBookingForm(@PathVariable Integer id, Model model) {
        BookingAdminDto booking = bookingService.getBookingById(id);
        model.addAttribute("booking", booking);
        return "admin/booking/edit";
    }

    @PostMapping("/bookings/{id}")
    public String updateBooking(@PathVariable Integer id,
                                @RequestParam BookingStatus bookingStatus,
                                RedirectAttributes redirectAttributes) {
        try {
            bookingService.updateBookingStatus(id, bookingStatus);
            redirectAttributes.addFlashAttribute("success", "Бронирование #" + id + " обновлено");
        } catch (Exception e) {
            log.error("Error updating booking {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении: " + e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    // ==================== POLICIES ====================

    @GetMapping("/policies")
    public String policiesList(Model model) {
        List<PolicyAdminDto> policies = policyService.getAllPolicies();
        model.addAttribute("policies", policies);
        return "admin/policies/list";
    }

    @GetMapping("/policies/{id}")
    public String showChosenPolicy(@PathVariable Integer id, Model model) {
        PolicyDto policy = policyService.getChosenPolicyDto(id);
        model.addAttribute("policy", policy);
        return "admin/policies/privacy";
    }

    @GetMapping("/policies/create")
    public String createPolicyForm() {
        return "admin/policies/create";
    }

    @PostMapping("/policies")
    public String createPolicy(@Valid @ModelAttribute PolicyCreateDto dto,
                               RedirectAttributes redirectAttributes,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/policies/create";
        }

        try {
            policyService.createNewPolicyVersion(dto);
            redirectAttributes.addFlashAttribute("success", "Политика версии " + dto.getVersion() + " добавлена");
        } catch (Exception e) {
            log.error("Error creating policy", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }

        return "redirect:/admin/policies";
    }

    // ==================== ROUTES ====================

    @GetMapping("/routes")
    public String routesList(Model model) {
        List<RouteDto> routes = routeService.getAllRoutes();
        model.addAttribute("routes", routes);
        return "admin/routes/list";
    }

    @GetMapping("/routes/create")
    public String createRouteForm(Model model) {
        model.addAttribute("difficulties", ge.tsepesh.motorental.enums.Difficulty.values());
        return "admin/routes/create";
    }

    @PostMapping("/routes")
    public String createRoute(@Valid @ModelAttribute RouteCreateDto dto,
                              @RequestParam(value = "mapImage", required = false) MultipartFile mapImage,
                              RedirectAttributes redirectAttributes,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/routes/create";
        }

        try {
            routeService.createRoute(dto, mapImage);
            redirectAttributes.addFlashAttribute("success", "Маршрут \"" + dto.getName() + "\" создан");
        } catch (Exception e) {
            log.error("Error creating route", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/routes/create";
        }

        return "redirect:/admin/routes";
    }

    @GetMapping("/routes/{id}/edit")
    public String editRouteForm(@PathVariable Integer id, Model model) {
        Route route = routeService.getRouteById(id);
        RouteUpdateDto dto = routeService.convertToUpdateDto(route);

        model.addAttribute("route", dto);
        model.addAttribute("difficulties", ge.tsepesh.motorental.enums.Difficulty.values());
        return "admin/routes/edit";
    }

    @PostMapping("/routes/{id}")
    public String updateRoute(@PathVariable Integer id,
                              @Valid @ModelAttribute RouteUpdateDto dto,
                              @RequestParam(value = "mapImage", required = false) MultipartFile mapImage,
                              RedirectAttributes redirectAttributes,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/routes/" + id + "/edit";
        }

        try {
            routeService.updateRoute(id, dto, mapImage);
            redirectAttributes.addFlashAttribute("success", "Маршрут обновлён");
        } catch (Exception e) {
            log.error("Error updating route {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/routes/" + id + "/edit";
        }

        return "redirect:/admin/routes";
    }

    @PostMapping("/routes/{id}/delete")
    public String deleteRoute(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            routeService.deleteRoute(id);
            redirectAttributes.addFlashAttribute("success", "Маршрут удалён");
        } catch (Exception e) {
            log.error("Error deleting route {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/routes";
    }

    // ==================== BIKES ====================

    @GetMapping("/bikes")
    public String bikesList(Model model) {
        model.addAttribute("bikes", bikeService.getAllBikesForAdmin());
        return "admin/bikes/list";
    }

    @GetMapping("/bikes/create")
    public String createBikeForm(Model model) {
        model.addAttribute("limits", limitService.getAllLimits());
        model.addAttribute("transmissionTypes", TransmissionType.values());
        return "admin/bikes/create";
    }

    @PostMapping("/bikes")
    public String createBike(@Valid @ModelAttribute BikeCreateDto dto,
                             BindingResult bindingResult,
                             @RequestParam(value = "bikePhoto", required = false) MultipartFile bikePhoto,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/bikes/create";
        }
        try {
            bikeService.createBike(dto, bikePhoto);
            redirectAttributes.addFlashAttribute("success",
                    "Мотоцикл \"" + dto.getBrand() + " " + dto.getModel() + "\" создан");
        } catch (Exception e) {
            log.error("Error creating bike", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/bikes/create";
        }
        return "redirect:/admin/bikes";
    }

    @GetMapping("/bikes/{id}/edit")
    public String editBikeForm(@PathVariable Integer id, Model model) {
        Bike bike = bikeService.getBikeEntityById(id);
        model.addAttribute("bike", bikeService.convertToUpdateDto(bike));
        model.addAttribute("limits", limitService.getAllLimits());
        model.addAttribute("transmissionTypes", TransmissionType.values());
        return "admin/bikes/edit";
    }

    @PostMapping("/bikes/{id}")
    public String updateBike(@PathVariable Integer id,
                             @Valid @ModelAttribute BikeUpdateDto dto,
                             BindingResult bindingResult,
                             @RequestParam(value = "bikePhoto", required = false) MultipartFile bikePhoto,
                             RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/bikes/" + id + "/edit";
        }
        try {
            bikeService.updateBike(id, dto, bikePhoto);
            redirectAttributes.addFlashAttribute("success", "Мотоцикл обновлён");
        } catch (Exception e) {
            log.error("Error updating bike {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/bikes/" + id + "/edit";
        }
        return "redirect:/admin/bikes";
    }

    @PostMapping("/bikes/{id}/delete")
    public String deleteBike(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            bikeService.deleteBike(id);
            redirectAttributes.addFlashAttribute("success", "Мотоцикл удалён");
        } catch (Exception e) {
            log.error("Error deleting bike {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/bikes";
    }

    // ==================== BANNERS ====================
    @GetMapping("/banner/list")
    public String bannerList(Model model) {
        List<BannerDto> banners = bannerService.getAllBanners();
        model.addAttribute("banners", banners);
        return "admin/banner/list";
    }
    @GetMapping("/banner/create")
    public String createBannerForm(Model model) {
        // Получить только специальные маршруты
        List<RouteDto> specialRoutes = routeService.getAllRoutes()
                .stream()
                .filter(r -> Boolean.TRUE.equals(r.getIsSpecial()) && Boolean.TRUE.equals(r.getIsEnabled()))
                .collect(Collectors.toList());
        if (specialRoutes.isEmpty()) {
            model.addAttribute("error", "Нет доступных специальных маршрутов. Сначала создайте маршрут с флагом 'Специальный'");
        }
        List<Shift> shifts = shiftService.getAllShifts();
        model.addAttribute("specialRoutes", specialRoutes);
        model.addAttribute("shifts", shifts);

        return "admin/banner/create";
    }
    @PostMapping("/banner")
    public String createBanner(@Valid @ModelAttribute BannerCreateDto dto,
                               @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage,
                               RedirectAttributes redirectAttributes,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/banner/create";
        }
        try {
            Banner created = bannerService.createBanner(dto, bannerImage);
            String message = Boolean.TRUE.equals(dto.getEnabled())
                    ? "Баннер #" + created.getId() + " создан и включен"
                    : "Баннер #" + created.getId() + " создан";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            log.error("Error creating banner", e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/banner/create";
        }
        return "redirect:/admin/banner/list";
    }
    @GetMapping("/banner/{id}/edit")
    public String editBannerForm(@PathVariable Integer id, Model model) {
        try {
            Banner banner = bannerService.getBannerById(id);
            BannerUpdateDto dto = bannerService.convertToUpdateDto(banner);
            // Получить специальные маршруты
            List<RouteDto> specialRoutes = routeService.getAllRoutes()
                    .stream()
                    .filter(r -> Boolean.TRUE.equals(r.getIsSpecial()) && Boolean.TRUE.equals(r.getIsEnabled()))
                    .collect(Collectors.toList());
            List<Shift> shifts = shiftService.getAllShifts();
            model.addAttribute("banner", dto);
            model.addAttribute("specialRoutes", specialRoutes);
            model.addAttribute("shifts", shifts);
            return "admin/banner/edit";
        } catch (Exception e) {
            log.error("Error loading banner edit form for id {}", id, e);
            return "redirect:/admin/banner/list";
        }
    }
    @PostMapping("/banner/{id}")
    public String updateBanner(@PathVariable Integer id,
                               @Valid @ModelAttribute BannerUpdateDto dto,
                               @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage,
                               RedirectAttributes redirectAttributes,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Ошибка валидации полей формы");
            return "redirect:/admin/banner/" + id + "/edit";
        }
        try {
            bannerService.updateBanner(id, dto, bannerImage);
            redirectAttributes.addFlashAttribute("success", "Баннер #" + id + " обновлен");
        } catch (Exception e) {
            log.error("Error updating banner {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
            return "redirect:/admin/banner/" + id + "/edit";
        }
        return "redirect:/admin/banner/list";
    }
    @PostMapping("/banner/{id}/toggle")
    public String toggleBanner(@PathVariable Integer id,
                               @RequestParam Boolean enabled,
                               RedirectAttributes redirectAttributes) {
        try {
            bannerService.toggleBanner(id, enabled);
            String message = enabled ? "Баннер включен" : "Баннер выключен";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            log.error("Error toggling banner {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/banner/list";
    }
    @PostMapping("/banner/{id}/delete")
    public String deleteBanner(@PathVariable Integer id,
                               RedirectAttributes redirectAttributes) {
        try {
            bannerService.deleteBanner(id);
            redirectAttributes.addFlashAttribute("success", "Баннер удален");
        } catch (Exception e) {
            log.error("Error deleting banner {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Ошибка: " + e.getMessage());
        }
        return "redirect:/admin/banner/list";
    }
}