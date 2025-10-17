package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.BikeAvailabilityDto;
import ge.tsepesh.motorental.dto.LimitDto;
import ge.tsepesh.motorental.model.Bike;
import ge.tsepesh.motorental.model.Limit;
import ge.tsepesh.motorental.repository.BikeRepository;
import ge.tsepesh.motorental.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BikeAvailabilityService {
    //ToDo Почистить комменты
    //ToDo Проверить код

    private final BikeRepository bikeRepository;
    private final ParticipantRepository participantRepository;

    @Transactional(readOnly = true)
    public List<BikeAvailabilityDto> getAvailableBikesForDateAndShift(LocalDate date, Integer shiftId) {
        List<Bike> allBikes = bikeRepository.findAll();
        List<Integer> occupiedBikeIds = participantRepository.findOccupiedBikeIds(date, shiftId);
        return allBikes.stream()
                .filter(bike -> !occupiedBikeIds.contains(bike.getId()))
                .map(this::mapToBikeAvailabilityDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Integer, Long> getAvailableBikesCountByLimitForDateAndShift(LocalDate date, Integer shiftId) {
        List<BikeAvailabilityDto> availableBikes = getAvailableBikesForDateAndShift(date, shiftId);
        
        return availableBikes.stream()
                .collect(Collectors.groupingBy(
                    BikeAvailabilityDto::getLimitId,
                    Collectors.counting()
                ));
    }

    @Transactional(readOnly = true)
    public long getTotalAvailableBikesForDateAndShift(LocalDate date, Integer shiftId) {
        long totalBikes = bikeRepository.count();
        long occupiedBikes = participantRepository.countByDateAndShift(date, shiftId);
        return totalBikes - occupiedBikes;
    }

    private BikeAvailabilityDto mapToBikeAvailabilityDto(Bike bike) {
        return BikeAvailabilityDto.builder()
                .id(bike.getId())
                .brand(bike.getBrand())
                .model(bike.getModel())
                .engineCc(bike.getEngineCc())
                .photoPath(bike.getPhotoPath())
                .limitId(bike.getLimits().getId())
                .limit(mapToLimitDto(bike.getLimits()))
                .build();
    }

    private LimitDto mapToLimitDto(Limit limit) {
        return LimitDto.builder()
                .id(limit.getId())
                .heightMin(limit.getHeightMin())
                .heightMax(limit.getHeightMax())
                .ageMin(limit.getAgeMin())
                .onlyMen(limit.getOnlyMen())
                .build();
    }
}


















