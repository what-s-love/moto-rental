package ge.tsepesh.motorental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransmissionType {
    MT(0,"bike.transmission.type.mt"),
    AMT(1,"bike.transmission.type.amt"),
    AT(2,"bike.transmission.type.at");

    private final int value;

    private final String displayName;

    public static TransmissionType fromValue(int value) {
        for (TransmissionType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transmission type value: " + value);
    }
}
