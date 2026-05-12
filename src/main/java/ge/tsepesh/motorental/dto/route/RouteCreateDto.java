package ge.tsepesh.motorental.dto.route;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RouteCreateDto {
    @NotBlank(message = "Название обязательно")
    @Size(max = 255)
    private String name;

    @NotNull(message = "Дистанция обязательна")
    @Min(value = 1, message = "Дистанция должна быть больше 0")
    private Integer distance;

    @NotNull(message = "Длительность обязательна")
    @Min(value = 1, message = "Длительность должна быть больше 0")
    private Integer duration;

    @NotNull(message = "Сложность обязательна")
    @Min(value = 0)
    private Integer difficulty;

    @NotNull(message = "Цена обязательна")
    @Min(value = 0)
    private BigDecimal price;

    @NotNull(message = "Цена в выходной обязательна")
    @Min(value = 0)
    private BigDecimal weekendPrice;

    @Size(max = 1000)
    private String description;

    private Boolean isSpecial;

    private Boolean enabled;
}