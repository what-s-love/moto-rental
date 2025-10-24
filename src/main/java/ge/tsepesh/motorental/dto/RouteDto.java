package ge.tsepesh.motorental.dto;

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
    private Difficulty difficulty;
    private String difficultyDisplayName;
    private BigDecimal price;
    private String mapPath;
    private String description;
    private Integer estimatedDuration; // в минутах
    private Boolean isAvailableForBeginners;
}