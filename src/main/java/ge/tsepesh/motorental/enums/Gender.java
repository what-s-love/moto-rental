package ge.tsepesh.motorental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("Мужской"),
    FEMALE("Женский");

    private final String displayName;
}