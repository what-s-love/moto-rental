package ge.tsepesh.motorental.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSettingDto {
    private Integer id;
    private String key;
    private String name;
    private String description;
    private String value;
}