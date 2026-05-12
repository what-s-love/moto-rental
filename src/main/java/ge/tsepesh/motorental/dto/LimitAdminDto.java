package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LimitAdminDto {
    private Integer id;
    private Integer heightMin;
    private Integer heightMax;
    private Integer ageMin;
    private Boolean onlyMen;
    private Integer bikesCount;  // Количество мотоциклов с этим лимитом
    private List<String> bikeNames;  // Список названий мотоциклов
}