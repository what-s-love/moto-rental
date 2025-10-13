package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.RouteDto;
import ge.tsepesh.motorental.enums.Difficulty;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouteService {
    private final RouteRepository routeRepository;

    public List<RouteDto> getAllActiveRoutes() {
        log.debug("Fetching all active routes");
        return routeRepository.findAll().stream()
                .filter(Route::getEnabled)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private RouteDto convertToDto(Route route) {
        Difficulty difficulty = Difficulty.values()[route.getDifficulty()];
        
        return RouteDto.builder()
                .id(route.getId())
                .distance(route.getDistance())
                .difficulty(difficulty)
                .difficultyDisplayName(difficulty.getDisplayName())
                .price(route.getPrice())
                .mapPath(route.getMapPath() != null ? route.getMapPath() : "/images/routes/no_route.jpg")
                .description(route.getDescription())
                .estimatedDuration(route.getDuration())
                .isAvailableForBeginners(difficulty.getValue() <= Difficulty.EASY.getValue())
                .build();
    }
}
