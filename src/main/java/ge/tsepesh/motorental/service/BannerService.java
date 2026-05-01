package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.BannerCreateDto;
import ge.tsepesh.motorental.dto.BannerDto;
import ge.tsepesh.motorental.dto.BannerUpdateDto;
import ge.tsepesh.motorental.model.Banner;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.BannerRepository;
import ge.tsepesh.motorental.repository.RouteRepository;
import ge.tsepesh.motorental.repository.ShiftRepository;
import ge.tsepesh.motorental.util.FileUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BannerService {

    private final BannerRepository bannerRepository;
    private final RouteRepository routeRepository;
    private final ShiftRepository shiftRepository;

    /**
     * Получить активный баннер для отображения на главной странице
     */
    public Optional<BannerDto> getActiveBanner() {
        log.debug("Fetching active banner");
        return bannerRepository.findActiveBanner()
                .map(this::convertToDto);
    }

    /**
     * Получить все баннеры для админки
     */
    public List<BannerDto> getAllBanners() {
        log.debug("Fetching all banners for admin");
        return bannerRepository.findAllOrderByEnabledAndCreatedAt()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить баннер по ID
     */
    public Banner getBannerById(Integer id) {
        return bannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Banner not found: " + id));
    }

    /**
     * Получить баннер по ID в виде DTO
     */
    public BannerDto getBannerDtoById(Integer id) {
        Banner banner = getBannerById(id);
        return convertToDto(banner);
    }

    /**
     * Создать новый баннер
     */
    @Transactional
    public Banner createBanner(BannerCreateDto dto, MultipartFile bannerImage) {
        log.info("Creating new banner: {}", dto.getTitle());

        // Валидация маршрута
        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + dto.getRouteId()));

        if (!route.getIsSpecial()) {
            throw new IllegalArgumentException("Only special routes can be used for banners. Route ID: " + dto.getRouteId());
        }

        if (!route.getEnabled()) {
            throw new IllegalArgumentException("Route must be enabled. Route ID: " + dto.getRouteId());
        }

        // Валидация смены
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + dto.getShiftId()));

        // Если баннер должен быть включен, выключаем все остальные
        if (Boolean.TRUE.equals(dto.getEnabled())) {
            bannerRepository.disableAllBanners();
            log.info("Disabled all existing banners");
        }

        // Создание баннера
        Banner banner = new Banner();
        banner.setRoute(route);
        banner.setRideDate(dto.getRideDate());
        banner.setShift(shift);
        banner.setTitle(dto.getTitle());
        banner.setDescription(dto.getDescription());
        banner.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);
        banner.setCreatedAt(LocalDateTime.now());

        // Сохранение изображения
        if (bannerImage != null && !bannerImage.isEmpty()) {
            String imagePath = saveBannerImage(bannerImage);
            banner.setImagePath(imagePath);
        } else {
            // Дефолтное изображение или изображение маршрута
            banner.setImagePath(route.getMapPath());
        }

        Banner saved = bannerRepository.save(banner);
        log.info("Banner created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Обновить баннер
     */
    @Transactional
    public Banner updateBanner(Integer id, BannerUpdateDto dto, MultipartFile bannerImage) {
        log.info("Updating banner: {}", id);

        Banner banner = getBannerById(id);

        // Валидация маршрута
        Route route = routeRepository.findById(dto.getRouteId())
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + dto.getRouteId()));

        if (!route.getIsSpecial()) {
            throw new IllegalArgumentException("Only special routes can be used for banners");
        }

        if (!route.getEnabled()) {
            throw new IllegalArgumentException("Route must be enabled");
        }

        // Валидация смены
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + dto.getShiftId()));

        // Если баннер включается, выключаем все остальные
        boolean wasDisabled = !banner.getEnabled();
        boolean willBeEnabled = Boolean.TRUE.equals(dto.getEnabled());

        if (wasDisabled && willBeEnabled) {
            bannerRepository.disableAllBannersExcept(id);
            log.info("Disabled all banners except {}", id);
        }

        // Обновление полей
        banner.setRoute(route);
        banner.setRideDate(dto.getRideDate());
        banner.setShift(shift);
        banner.setTitle(dto.getTitle());
        banner.setDescription(dto.getDescription());
        banner.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);
        banner.setUpdatedAt(LocalDateTime.now());

        // Обновление изображения, если предоставлено новое
        if (bannerImage != null && !bannerImage.isEmpty()) {
            // Удаляем старое изображение (если оно не от маршрута)
            if (banner.getImagePath() != null
                    && !banner.getImagePath().equals(route.getMapPath())
                    && !banner.getImagePath().equals("/images/banners/default.jpg")) {
                deleteBannerImage(banner.getImagePath());
            }

            String imagePath = saveBannerImage(bannerImage);
            banner.setImagePath(imagePath);
        }

        Banner updated = bannerRepository.save(banner);
        log.info("Banner {} updated successfully", id);
        return updated;
    }

    /**
     * Переключить статус баннера (включить/выключить)
     */
    @Transactional
    public Banner toggleBanner(Integer id, Boolean enabled) {
        log.info("Toggling banner {} to {}", id, enabled);

        Banner banner = getBannerById(id);

        // Если включаем баннер, выключаем все остальные
        if (Boolean.TRUE.equals(enabled)) {
            bannerRepository.disableAllBannersExcept(id);
            log.info("Disabled all banners except {}", id);
        }

        banner.setEnabled(enabled);
        banner.setUpdatedAt(LocalDateTime.now());

        Banner updated = bannerRepository.save(banner);
        log.info("Banner {} toggled to {}", id, enabled);
        return updated;
    }

    /**
     * Удалить баннер
     */
    @Transactional
    public void deleteBanner(Integer id) {
        log.info("Deleting banner: {}", id);

        Banner banner = getBannerById(id);

        // Удаляем изображение, если оно не дефолтное и не от маршрута
        if (banner.getImagePath() != null
                && !banner.getImagePath().equals(banner.getRoute().getMapPath())
                && !banner.getImagePath().equals("/images/banners/default.jpg")) {
            deleteBannerImage(banner.getImagePath());
        }

        bannerRepository.delete(banner);
        log.info("Banner {} deleted", id);
    }

    /**
     * Конвертировать Banner в BannerDto
     */
    public BannerDto convertToDto(Banner banner) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        return BannerDto.builder()
                .id(banner.getId())
                .enabled(banner.getEnabled())
                .routeId(banner.getRoute().getId())
                .routeName(banner.getRoute().getName())
                .rideDate(banner.getRideDate())
                .shiftId(banner.getShift().getId())
                .shiftName(banner.getShift().getName())
                .shiftStartTime(banner.getShift().getStartTime().format(timeFormatter))
                .shiftEndTime(banner.getShift().getEndTime().format(timeFormatter))
                .title(banner.getTitle())
                .description(banner.getDescription())
                .imagePath(banner.getImagePath() != null
                        ? banner.getImagePath()
                        : "/images/banners/default.jpg")
                .build();
    }

    /**
     * Конвертировать Banner в BannerUpdateDto для редактирования
     */
    public BannerUpdateDto convertToUpdateDto(Banner banner) {
        return BannerUpdateDto.builder()
                .id(banner.getId())
                .routeId(banner.getRoute().getId())
                .rideDate(banner.getRideDate())
                .shiftId(banner.getShift().getId())
                .title(banner.getTitle())
                .description(banner.getDescription())
                .imagePath(banner.getImagePath())
                .enabled(banner.getEnabled())
                .build();
    }

    /**
     * Получить статистику по баннерам
     */
    public BannerStatsDto getBannerStats() {
        long total = bannerRepository.count();
        long enabled = bannerRepository.countEnabledBanners();
        long disabled = total - enabled;

        return BannerStatsDto.builder()
                .totalBanners(total)
                .enabledBanners(enabled)
                .disabledBanners(disabled)
                .build();
    }

    // ==================== FILE UPLOAD METHODS ====================

    /**
     * Сохранить изображение баннера
     */
    private String saveBannerImage(MultipartFile file) {
        try {
            String savedPath = FileUtil.saveUploadedFile(file, "images/banners");
            String fileName = Paths.get(savedPath).getFileName().toString();
            String webPath = "/images/banners/" + fileName;

            log.info("Banner image saved: {}", webPath);
            return webPath;

        } catch (Exception e) {
            log.error("Error saving banner image", e);
            throw new RuntimeException("Failed to save banner image: " + e.getMessage());
        }
    }

    /**
     * Удалить изображение баннера
     */
    private void deleteBannerImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty()
                    || imagePath.equals("/images/banners/default.jpg")) {
                return;
            }

            String fileName = imagePath.replace("/images/banners/", "");
            Path filePath = Paths.get("data/images/banners", fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Banner image deleted: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Error deleting banner image: {}", imagePath, e);
            // Не бросаем exception - удаление картинки не критично
        }
    }

    // ==================== HELPER DTO ====================

    /**
     * DTO для статистики баннеров
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BannerStatsDto {
        private long totalBanners;
        private long enabledBanners;
        private long disabledBanners;
    }
}