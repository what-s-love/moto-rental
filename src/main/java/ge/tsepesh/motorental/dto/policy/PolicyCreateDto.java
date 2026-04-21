package ge.tsepesh.motorental.dto.policy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PolicyCreateDto {
    @NotBlank
    private String version;

    @NotBlank
    @Size(max = 10000)
    private String text;
}