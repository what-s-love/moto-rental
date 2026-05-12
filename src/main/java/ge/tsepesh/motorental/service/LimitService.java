package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.LimitAdminDto;
import ge.tsepesh.motorental.dto.LimitCreateDto;
import ge.tsepesh.motorental.dto.LimitUpdateDto;
import ge.tsepesh.motorental.model.Limit;
import ge.tsepesh.motorental.repository.LimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LimitService {
    private final LimitRepository limitRepository;

    /**
     * Получить все лимиты для админки
     */
    public List<Limit> getAllLimits() {
        return limitRepository.findAll();
    }

    /**
     * Получить все лимиты с информацией о связанных мотоциклах
     */
    public List<LimitAdminDto> getAllLimitsWithBikesInfo() {
        List<Limit> limits = limitRepository.findAllWithBikes();
        return limits.stream()
                .map(this::mapToLimitAdminDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить лимит по ID
     */
    public Limit getLimitById(Integer id) {
        return limitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Limit not found: " + id));
    }

    /**
     * Обновить лимит
     */
    public Limit updateLimit(Integer id, LimitUpdateDto dto) {
        Limit limit = getLimitById(id);

        limit.setHeightMin(dto.getHeightMin());
        limit.setHeightMax(dto.getHeightMax());
        limit.setAgeMin(dto.getAgeMin());
        limit.setOnlyMen(dto.getOnlyMen() != null ? dto.getOnlyMen() : false);

        Limit updated = limitRepository.save(limit);
        log.info("Limit {} updated successfully", id);
        return updated;
    }

    /**
     * Создать новое ограничение
     */
    public Limit createLimit(LimitCreateDto dto) {
        log.info("Creating new limit: heightMin={}, heightMax={}, ageMin={}, onlyMen={}",
                dto.getHeightMin(), dto.getHeightMax(), dto.getAgeMin(), dto.getOnlyMen());

        Limit limit = new Limit();
        limit.setHeightMin(dto.getHeightMin());
        limit.setHeightMax(dto.getHeightMax());
        limit.setAgeMin(dto.getAgeMin());
        limit.setOnlyMen(dto.getOnlyMen() != null ? dto.getOnlyMen() : false);

        Limit saved = limitRepository.save(limit);
        log.info("Limit created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Массовое обновление лимитов
     */
    public void updateLimits(List<LimitUpdateDto> updates) {
        for (LimitUpdateDto dto : updates) {
            if (dto.getId() == null) {
                log.warn("LimitUpdateDto without id, skipping");
                continue;
            }

            Limit limit = limitRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Limit not found: " + dto.getId()));

            limit.setHeightMin(dto.getHeightMin());
            limit.setHeightMax(dto.getHeightMax());
            limit.setAgeMin(dto.getAgeMin());
            limit.setOnlyMen(dto.getOnlyMen() != null ? dto.getOnlyMen() : false);

            limitRepository.save(limit);
            log.info("Updated limit {} (heightMin={}, heightMax={}, ageMin={}, onlyMen={})",
                    limit.getId(), limit.getHeightMin(), limit.getHeightMax(),
                    limit.getAgeMin(), limit.getOnlyMen());
        }
        log.info("Updated {} limits", updates.size());
    }

    /**
     * Конвертировать Limit в LimitAdminDto
     */
    private LimitAdminDto mapToLimitAdminDto(Limit limit) {
        List<String> bikeNames = limit.getBikes().stream()
                .map(bike -> bike.getBrand() + " " + bike.getModel())
                .collect(Collectors.toList());

        return LimitAdminDto.builder()
                .id(limit.getId())
                .heightMin(limit.getHeightMin())
                .heightMax(limit.getHeightMax())
                .ageMin(limit.getAgeMin())
                .onlyMen(limit.getOnlyMen())
                .bikesCount(limit.getBikes().size())
                .bikeNames(bikeNames)
                .build();
    }
}