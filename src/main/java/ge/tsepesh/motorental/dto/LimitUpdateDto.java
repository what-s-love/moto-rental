package ge.tsepesh.motorental.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class LimitUpdateDto {
    @NotNull
    private Integer id;

    @NotNull(message = "Минимальный рост обязателен")
    @Min(value = 100, message = "Минимальный рост должен быть не менее 100 см")
    private Integer heightMin;

    @NotNull(message = "Максимальный рост обязателен")
    @Min(value = 100, message = "Максимальный рост должен быть не менее 100 см")
    private Integer heightMax;

    @NotNull(message = "Минимальный возраст обязателен")
    @Min(value = 1, message = "Минимальный возраст должен быть не менее 1 года")
    private Integer ageMin;

    private Boolean onlyMen;

    // ==================== ОБЁРТКА ДЛЯ СПИСКА ====================

    @Data
    public static class LimitsUpdateForm {
        private List<LimitUpdateDto> limits = new ArrayList<>();
    }
}