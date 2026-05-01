package ge.tsepesh.motorental.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDto {
    private Integer id;
    private Boolean enabled;
    private Integer routeId;
    private String routeName;
    private LocalDate rideDate;
    private Integer shiftId;
    private String shiftName;
    private String shiftStartTime;
    private String shiftEndTime;
    private String title;
    private String description;
    private String imagePath;
}