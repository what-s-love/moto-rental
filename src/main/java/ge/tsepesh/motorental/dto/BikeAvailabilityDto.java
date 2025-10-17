package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BikeAvailabilityDto {
    private Integer id;
    private String brand;
    private String model;
    private Integer engineCc;
    private String photoPath;
    private Integer limitId;
    private LimitDto limit;
}















