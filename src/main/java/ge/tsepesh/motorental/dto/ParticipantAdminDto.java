package ge.tsepesh.motorental.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipantAdminDto {
    private Integer id;
    private String bikeName;
    private String gender;
    private Integer age;
    private Integer height;
    private String experienceLevel;
}