package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
public class ShiftDto {
    private Integer id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
}
