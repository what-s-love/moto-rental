package ge.tsepesh.motorental.dto.bike;

import ge.tsepesh.motorental.dto.LimitDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BikeDto {
    private Integer id;
    private String brand;
    private String model;
    private Integer engineCc;
    private String transmissionType;
    private LimitDto limits;
    private String photoPath;
    private Boolean enabled;
}