package ge.tsepesh.motorental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceLevel {
    NONE("Не умею на велосипеде"),
    BEGINNER("Умею на велосипеде, не пробовал на мотоцикле"),
    INTERMEDIATE("Ездил на мотоцикле, но давно и не правда"),
    ADVANCED("Да я вообще мотоциклист");

    private final String displayName;
}