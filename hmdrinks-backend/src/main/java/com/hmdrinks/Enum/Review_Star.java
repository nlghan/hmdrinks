package com.hmdrinks.Enum;

import lombok.Getter;

@Getter
public enum Review_Star {
    ONE(1),
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5);

    private final int value;

    Review_Star(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

