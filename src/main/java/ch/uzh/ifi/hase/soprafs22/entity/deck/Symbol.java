package ch.uzh.ifi.hase.soprafs22.entity.deck;

import java.io.Serializable;

public enum Symbol implements Serializable {

    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6"),
    SEVEN("7"),
    EIGHT("8"),
    NINE("9"),
    TEN("10"),
    WILDCARD("Wildcard"),
    REVERSE("Reverse"),
    HIT_2("Hit 2"),
    SKIP("Skip"),
    DISCARD_ALL("Discard all"),
    EXTREME_HIT("Extreme Hit");

    private static final long serialVersionUID = 1L;

    String stringRepresentation;

    Symbol(String s) {
        stringRepresentation = s;
    }



    public String getString(){
        return stringRepresentation;
    }
}


