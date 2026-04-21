package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class ShiftDto {
    private Integer id;
    private String name;
    private String startTime;
    private String endTime;
    private String enabled;
}
