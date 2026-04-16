package ge.tsepesh.motorental.dto;

import lombok.Data;

import java.time.LocalTime;

@Data
public class ShiftUpdateDto {
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean enabled;
}