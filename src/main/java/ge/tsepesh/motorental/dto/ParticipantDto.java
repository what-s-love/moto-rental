package ge.tsepesh.motorental.dto;

import ge.tsepesh.motorental.enums.ExperienceLevel;
import ge.tsepesh.motorental.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParticipantDto {
    @NotNull(message = "Пол обязателен")
    private Gender gender;

    @NotNull(message = "Возраст обязателен")
    @Min(value = 12, message = "Минимальный возраст: 12 лет")
    @Max(value = 99, message = "Максимальный возраст: 99 лет")
    private Integer age;

    @NotNull(message = "Рост обязателен")
    @Min(value = 130, message = "Минимальный рост: 130 см")
    @Max(value = 220, message = "Максимальный рост: 220 см")
    private Integer height;

    @NotNull(message = "Уровень опыта обязателен")
    private ExperienceLevel experienceLevel;

    @NotNull(message = "Мотоцикл обязателен")
    @Min(value = 1, message = "Некорректный мотоцикл")
    private Integer bikeId;
}