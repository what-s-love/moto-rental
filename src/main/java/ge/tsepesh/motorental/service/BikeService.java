package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.BikeDto;
import ge.tsepesh.motorental.dto.LimitDto;
import ge.tsepesh.motorental.enums.TransmissionType;
import ge.tsepesh.motorental.model.Bike;
import ge.tsepesh.motorental.repository.BikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BikeService {
    private final BikeRepository bikeRepository;

    public List<BikeDto> getAllActiveBikes() {
        log.debug("Fetching all active bikes");
        return bikeRepository.findAll().stream()
                .filter(Bike::getEnabled)
                .map(this::convertToDto)
                .collect(Collectors.toList());
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

    private LimitDto convertLimitToDto(ge.tsepesh.motorental.model.Limit limit) {
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
}
