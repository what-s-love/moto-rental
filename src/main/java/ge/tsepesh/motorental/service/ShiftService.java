package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.ShiftCreateDto;
import ge.tsepesh.motorental.dto.ShiftDto;
import ge.tsepesh.motorental.dto.ShiftUpdateDto;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ShiftService {
    //ToDo Проверить код
    //ToDo Убрать комменты
    private final ShiftRepository shiftRepository;

    public List<Shift> getAllShifts() {
        return shiftRepository.findAll();
    }

    public List<ShiftDto> getEnabledShifts() {
        return shiftRepository.findEnabledShifts()
                .stream()
                .map(this::mapToShiftDto)
                .toList();
    }

    public Shift getShiftById(Integer id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + id));
    }

    public Shift updateShift(Integer id, ShiftUpdateDto dto) {
        Shift shift = getShiftById(id);

        if (dto.getName() != null) shift.setName(dto.getName());
        if (dto.getStartTime() != null) shift.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) shift.setEndTime(dto.getEndTime());
        if (dto.getEnabled() != null) shift.setEnabled(dto.getEnabled());

        return shiftRepository.save(shift);
    }

    public void toggleShiftEnabled(Integer id) {
        Shift shift = getShiftById(id);
        shift.setEnabled(!shift.getEnabled());
        shiftRepository.save(shift);
        log.info("Shift {} enabled status toggled to {}", id, shift.getEnabled());
    }

    /**
     * Создать новую смену
     */
    public Shift createShift(ShiftCreateDto dto) {
        log.info("Creating new shift: {}", dto.getName());

        Shift shift = new Shift();
        shift.setName(dto.getName());
        shift.setStartTime(dto.getStartTime());
        shift.setEndTime(dto.getEndTime());
        shift.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        Shift saved = shiftRepository.save(shift);
        log.info("Shift created with id: {}", saved.getId());
        return saved;
    }

    /**
     * Массовое обновление смен (для формы админки)
     */
    public void updateShifts(List<ShiftUpdateDto> updates) {
        for (ShiftUpdateDto dto : updates) {
            if (dto.getId() == null) {
                log.warn("ShiftUpdateDto without id, skipping");
                continue;
            }

            Shift shift = shiftRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Shift not found: " + dto.getId()));

            // Обновляем только те поля, которые переданы
            if (dto.getName() != null) {
                shift.setName(dto.getName());
            }
            if (dto.getStartTime() != null) {
                shift.setStartTime(dto.getStartTime());
            }
            if (dto.getEndTime() != null) {
                shift.setEndTime(dto.getEndTime());
            }
            if (dto.getEnabled() != null) {
                shift.setEnabled(dto.getEnabled());
            }

            shiftRepository.save(shift);
            log.info("Updated shift {} (enabled={})", shift.getId(), shift.getEnabled());
        }
        log.info("Updated {} shifts", updates.size());
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
}