package ge.tsepesh.motorental.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShiftUpdateDto {
    private Integer id;
    private String name;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean enabled;

    @Data
    public static class ShiftsUpdateForm {
        private List<ShiftUpdateDto> shifts = new ArrayList<>();
    }
}