package ge.tsepesh.motorental.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {
    INDIVIDUAL(0,"индивидуальный"),
    EASY(1,"легкий"),
    MEDIUM(2,"средний"),
    PRE_HARD(3,"умеренно сложный"),
    HARD(4,"сложный");

    private final int value;

    private final String displayName;
}