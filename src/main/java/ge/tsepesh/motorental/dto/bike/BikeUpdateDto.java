package ge.tsepesh.motorental.dto.bike;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BikeUpdateDto {

    @NotNull
    private Integer id;

    @NotBlank(message = "Марка обязательна")
    @Size(max = 100)
    private String brand;

    @NotBlank(message = "Модель обязательна")
    @Size(max = 100)
    private String model;

    @NotNull(message = "Объём двигателя обязателен")
    @Min(value = 1, message = "Объём должен быть больше 0")
    @Max(value = 5000)
    private Integer engineCc;

    @NotNull(message = "Тип трансмиссии обязателен")
    @Min(0)
    @Max(2)
    private Integer transmissionType;

    @NotNull(message = "Ограничение обязательно")
    private Integer limitId;

    private String photoPath;

    private Boolean enabled;
}
