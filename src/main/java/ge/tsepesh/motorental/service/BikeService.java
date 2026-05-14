package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.bike.BikeCreateDto;
import ge.tsepesh.motorental.dto.bike.BikeDto;
import ge.tsepesh.motorental.dto.bike.BikeUpdateDto;
import ge.tsepesh.motorental.dto.LimitDto;
import ge.tsepesh.motorental.enums.TransmissionType;
import ge.tsepesh.motorental.model.Bike;
import ge.tsepesh.motorental.model.Limit;
import ge.tsepesh.motorental.repository.BikeRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BikeService {

    private final BikeRepository bikeRepository;
    private final LimitService limitService;

    public List<BikeDto> getAllActiveBikes() {
        log.debug("Fetching all active bikes");
        return bikeRepository.findAll().stream()
                .filter(Bike::getEnabled)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ==================== ADMIN ====================

    public List<BikeDto> getAllBikesForAdmin() {
        log.debug("Fetching all bikes for admin");
        return bikeRepository.findAllWithLimits().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Bike getBikeEntityById(Integer id) {
        return bikeRepository.findByIdWithLimits(id)
                .orElseThrow(() -> new IllegalArgumentException("Bike not found: " + id));
    }

    public BikeUpdateDto convertToUpdateDto(Bike bike) {
        BikeUpdateDto dto = new BikeUpdateDto();
        dto.setId(bike.getId());
        dto.setBrand(bike.getBrand());
        dto.setModel(bike.getModel());
        dto.setEngineCc(bike.getEngineCc());
        dto.setTransmissionType(bike.getTransmissionType());
        dto.setLimitId(bike.getLimits() != null ? bike.getLimits().getId() : null);
        dto.setPhotoPath(bike.getPhotoPath());
        dto.setEnabled(bike.getEnabled());
        return dto;
    }

    @Transactional
    public Bike createBike(BikeCreateDto dto, MultipartFile bikePhoto) {
        log.info("Creating bike: {} {}", dto.getBrand(), dto.getModel());

        Limit limit = limitService.getLimitById(dto.getLimitId());

        Bike bike = new Bike();
        bike.setBrand(dto.getBrand().trim());
        bike.setModel(dto.getModel().trim());
        bike.setEngineCc(dto.getEngineCc());
        bike.setTransmissionType(dto.getTransmissionType());
        bike.setLimits(limit);
        bike.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        if (bikePhoto != null && !bikePhoto.isEmpty()) {
            bike.setPhotoPath(saveBikePhoto(bikePhoto));
        }

        Bike saved = bikeRepository.save(bike);
        log.info("Bike created with id: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Bike updateBike(Integer id, BikeUpdateDto dto, MultipartFile bikePhoto) {
        log.info("Updating bike: {}", id);

        Bike bike = getBikeEntityById(id);
        Limit limit = limitService.getLimitById(dto.getLimitId());

        bike.setBrand(dto.getBrand().trim());
        bike.setModel(dto.getModel().trim());
        bike.setEngineCc(dto.getEngineCc());
        bike.setTransmissionType(dto.getTransmissionType());
        bike.setLimits(limit);
        bike.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);

        if (bikePhoto != null && !bikePhoto.isEmpty()) {
            deleteBikePhotoFile(bike.getPhotoPath());
            bike.setPhotoPath(saveBikePhoto(bikePhoto));
        }

        Bike updated = bikeRepository.save(bike);
        log.info("Bike {} updated", id);
        return updated;
    }

    @Transactional
    public void deleteBike(Integer id) {
        Bike bike = getBikeEntityById(id);
        long participants = bikeRepository.countParticipantsByBikeId(id);
        if (participants > 0) {
            throw new IllegalStateException(
                    "Нельзя удалить мотоцикл: есть записи участников (" + participants + ")");
        }
        deleteBikePhotoFile(bike.getPhotoPath());
        bikeRepository.delete(bike);
        log.info("Bike {} deleted", id);
    }

    private BikeDto convertToDto(Bike bike) {
        return BikeDto.builder()
                .id(bike.getId())
                .brand(bike.getBrand())
                .model(bike.getModel())
                .engineCc(bike.getEngineCc())
                .transmissionType(TransmissionType.fromValue(bike.getTransmissionType()).getDisplayName())
                .limits(convertLimitToDto(bike.getLimits()))
                .photoPath(bike.getPhotoPath())
                .enabled(bike.getEnabled())
                .build();
    }

    private LimitDto convertLimitToDto(Limit limit) {
        if (limit == null) {
            return null;
        }
        return LimitDto.builder()
                .id(limit.getId())
                .heightMin(limit.getHeightMin())
                .heightMax(limit.getHeightMax())
                .ageMin(limit.getAgeMin())
                .onlyMen(limit.getOnlyMen())
                .build();
    }

    private String saveBikePhoto(MultipartFile file) {
        try {
            String savedPath = FileUtil.saveUploadedFile(file, "images/bikes");
            String fileName = Paths.get(savedPath).getFileName().toString();
            String webPath = "/images/bikes/" + fileName;
            log.info("Bike photo saved: {}", webPath);
            return webPath;
        } catch (Exception e) {
            log.error("Error saving bike photo", e);
            throw new RuntimeException("Не удалось сохранить фото: " + e.getMessage());
        }
    }

    private void deleteBikePhotoFile(String imagePath) {
        try {
            if (imagePath == null || imagePath.isBlank() || !imagePath.startsWith("/images/bikes/")) {
                return;
            }
            String fileName = imagePath.replace("/images/bikes/", "");
            Path filePath = Paths.get("data/images/bikes", fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Bike photo deleted: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.warn("Could not delete bike photo: {}", imagePath, e);
        }
    }
}
