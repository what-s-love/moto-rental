package ge.tsepesh.motorental.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerCreateDto {

    @NotNull(message = "Необходимо выбрать маршрут")
    private Integer routeId;

    @NotNull(message = "Необходимо выбрать дату")
    private LocalDate rideDate;

    @NotNull(message = "Необходимо выбрать смену")
    private Integer shiftId;

    @NotBlank(message = "Заголовок обязателен")
    private String title;

    private String description;

    private Boolean enabled;
}