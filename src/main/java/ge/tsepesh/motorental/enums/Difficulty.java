package ge.tsepesh.motorental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {
    EASY("Легкий"),
    MEDIUM("Средний"),
    HARD("Жесткий");

    private final String displayName;
}