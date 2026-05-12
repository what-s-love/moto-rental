package ge.tsepesh.motorental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ShiftCreateDto {
    @NotBlank(message = "Название обязательно")
    @Size(max = 50, message = "Название не должно превышать 50 символов")
    private String name;

    @NotNull(message = "Время начала обязательно")
    private LocalTime startTime;

    @NotNull(message = "Время окончания обязательно")
    private LocalTime endTime;

    private Boolean enabled;
}