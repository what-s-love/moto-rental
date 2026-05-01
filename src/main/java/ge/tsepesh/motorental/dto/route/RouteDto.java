package ge.tsepesh.motorental.dto.route;

import ge.tsepesh.motorental.enums.Difficulty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RouteDto {
    private Integer id;
    private String name;
    private Integer distance;
    private Integer estimatedDuration; // в минутах
    private Difficulty difficulty;
    private String difficultyDisplayName;
    private BigDecimal price;
    private BigDecimal weekendPrice;
    private String description;
    private String mapPath;
    private Boolean isAvailableForBeginners;
    private Boolean isSpecial;
    private Boolean isEnabled;
}