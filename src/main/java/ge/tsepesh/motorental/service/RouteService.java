package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.route.RouteCreateDto;
import ge.tsepesh.motorental.dto.route.RouteDto;
import ge.tsepesh.motorental.dto.route.RouteUpdateDto;
import ge.tsepesh.motorental.enums.Difficulty;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.repository.RouteRepository;
import ge.tsepesh.motorental.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {
    private final RouteRepository routeRepository;

    public List<RouteDto> getAllActiveRoutes() {
        log.debug("Fetching all active routes");
        return routeRepository.findActiveNonSpecialRoutes()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ==================== ADMIN METHODS ====================
    /**
     * Получить все маршруты (включая отключенные) для админки
     */
    public List<RouteDto> getAllRoutes() {
        log.debug("Fetching all routes for admin");
        return routeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить маршрут по ID
     */
    public Route getRouteById(Integer id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
    }

    /**
     * Создать новый маршрут
     */
    @Transactional
    public Route createRoute(RouteCreateDto dto, MultipartFile mapImage) {
        log.info("Creating new route: {}", dto.getName());

        Route route = new Route();
        route.setName(dto.getName());
        route.setDistance(dto.getDistance());
        route.setDuration(dto.getDuration());
        route.setDifficulty(dto.getDifficulty());
        route.setPrice(dto.getPrice());
        route.setWeekendPrice(dto.getWeekendPrice());
        route.setDescription(dto.getDescription());
        route.setIsSpecial(dto.getIsSpecial() != null ? dto.getIsSpecial() : false);
        route.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        // Сохраняем картинку, если предоставлена
        if (mapImage != null && !mapImage.isEmpty()) {
            String imagePath = saveRouteImage(mapImage);
            route.setMapPath(imagePath);
        } else {
            route.setMapPath("/images/routes/no_route.jpg");
        }

        Route saved = routeRepository.save(route);
        log.info("Route created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Обновить маршрут
     */
    @Transactional
    public Route updateRoute(Integer id, RouteUpdateDto dto, MultipartFile mapImage) {
        log.info("Updating route: {}", id);
        log.info("Incoming dto: {}", dto.toString());

        Route route = getRouteById(id);

        route.setName(dto.getName());
        route.setDistance(dto.getDistance());
        route.setDuration(dto.getDuration());
        route.setDifficulty(dto.getDifficulty());
        route.setPrice(dto.getPrice());
        route.setWeekendPrice(dto.getWeekendPrice());
        route.setDescription(dto.getDescription());
        route.setIsSpecial(dto.getIsSpecial() != null ? dto.getIsSpecial() : false);
        route.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);

        // Если предоставлена новая картинка - сохраняем
        if (mapImage != null && !mapImage.isEmpty()) {
            // Удаляем старую картинку (если не дефолтная)
            if (route.getMapPath() != null && !route.getMapPath().equals("/images/routes/no_route.jpg")) {
                deleteRouteImage(route.getMapPath());
            }

            String imagePath = saveRouteImage(mapImage);
            route.setMapPath(imagePath);
        }

        Route updated = routeRepository.save(route);
        log.info("Route {} updated successfully", id);
        return updated;
    }

    /**
     * Удалить маршрут
     */
    @Transactional
    public void deleteRoute(Integer id) {
        Route route = getRouteById(id);

        // Удаляем картинку
        if (route.getMapPath() != null && !route.getMapPath().equals("/images/routes/no_route.jpg")) {
            deleteRouteImage(route.getMapPath());
        }

        routeRepository.delete(route);
        log.info("Route {} deleted", id);
    }

    /**
     * Конвертировать Route в RouteUpdateDto для редактирования
     */
    public RouteUpdateDto convertToUpdateDto(Route route) {
        RouteUpdateDto dto = new RouteUpdateDto();
        dto.setId(route.getId());
        dto.setName(route.getName());
        dto.setDistance(route.getDistance());
        dto.setDuration(route.getDuration());
        dto.setDifficulty(route.getDifficulty());
        dto.setPrice(route.getPrice());
        dto.setWeekendPrice(route.getWeekendPrice());
        dto.setDescription(route.getDescription());
        dto.setMapPath(route.getMapPath());
        dto.setIsSpecial(route.getIsSpecial());
        dto.setEnabled(route.getEnabled());
        return dto;
    }

    // ==================== FILE UPLOAD METHODS ====================
    /**
     * Сохранить изображение маршрута (используя FileUtil)
     */
    private String saveRouteImage(MultipartFile file) {
        try {
            // FileUtil.saveUploadedFile сохраняет в data/{subDir}/
            String savedPath = FileUtil.saveUploadedFile(file, "images/routes");

            // savedPath будет вида: "data/images/routes/{uuid}_{filename}"
            // Нам нужно вернуть путь вида "/images/routes/{uuid}_{filename}"
            // Извлекаем только имя файла
            String fileName = Paths.get(savedPath).getFileName().toString();
            String webPath = "/images/routes/" + fileName;

            log.info("Route image saved: {}", webPath);
            return webPath;

        } catch (Exception e) {
            log.error("Error saving route image", e);
            throw new RuntimeException("Failed to save route image: " + e.getMessage());
        }
    }
    /**
     * Удалить изображение маршрута
     */
    private void deleteRouteImage(String imagePath) {
        try {
            if (imagePath == null || imagePath.isEmpty() || imagePath.equals("/images/routes/no_route.jpg")) {
                return;
            }

            // imagePath вида "/images/routes/{uuid}_{filename}"
            // Убираем /images/routes/ и получаем имя файла
            String fileName = imagePath.replace("/images/routes/", "");
            Path filePath = Paths.get("data/images/routes", fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Route image deleted: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Error deleting route image: {}", imagePath, e);
            // Не бросаем exception - удаление картинки не критично
        }
    }

    public RouteDto convertToDto(Route route) {
        Difficulty difficulty = Difficulty.values()[route.getDifficulty()];
        
        return RouteDto.builder()
                .id(route.getId())
                .name(route.getName())
                .distance(route.getDistance())
                .difficulty(difficulty)
                .difficultyDisplayName(difficulty.getDisplayName())
                .price(route.getPrice())
                .weekendPrice(route.getWeekendPrice())
                .mapPath(route.getMapPath() != null ? route.getMapPath() : "/images/routes/no_route.jpg")
                .description(route.getDescription())
                .estimatedDuration(route.getDuration())
                .isAvailableForBeginners(difficulty.getValue() <= Difficulty.EASY.getValue())
                .isSpecial(route.getIsSpecial())
                .isEnabled(route.getEnabled())
                .build();
    }
}
