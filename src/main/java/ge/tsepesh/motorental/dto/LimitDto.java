package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LimitDto {
    private Integer id;
    private Integer heightMin;
    private Integer heightMax;
    private Integer ageMin;
    private Boolean onlyMen;
    private String description;
}
