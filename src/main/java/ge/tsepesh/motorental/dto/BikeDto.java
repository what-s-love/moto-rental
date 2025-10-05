package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BikeDto {
    private Integer id;
    private String brand;
    private String model;
    private Integer engineCc;
    private String photoPath;
    private String description;
    private LimitDto limits;
    private Boolean isAvailable;
}