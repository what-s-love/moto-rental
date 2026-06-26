package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.settings.AppSettingDto;
import ge.tsepesh.motorental.enums.AppSettingKey;
import ge.tsepesh.motorental.exception.ResourceNotFoundException;
import ge.tsepesh.motorental.model.AppSetting;
import ge.tsepesh.motorental.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppSettingService {

    private final AppSettingRepository appSettingRepository;

    public List<AppSettingDto> getAllSettings() {
        return appSettingRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public String getValue(AppSettingKey key) {
        return appSettingRepository.findByKey(key.name())
                .map(AppSetting::getValue)
                .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + key.name()));
    }

    public String getValueOrDefault(AppSettingKey key, String defaultValue) {
        return appSettingRepository.findByKey(key.name())
                .map(AppSetting::getValue)
                .orElse(defaultValue);
    }

    @Transactional
    public void updateAll(List<AppSettingDto> dtos) {
        for (AppSettingDto dto : dtos) {
            AppSetting setting = appSettingRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Setting not found: " + dto.getId()));
            setting.setValue(dto.getValue());
            appSettingRepository.save(setting);
        }
        log.info("Updated {} settings", dtos.size());
    }

    private AppSettingDto toDto(AppSetting setting) {
        return AppSettingDto.builder()
                .id(setting.getId())
                .key(setting.getKey())
                .name(setting.getName())
                .description(setting.getDescription())
                .value(setting.getValue())
                .build();
    }
}