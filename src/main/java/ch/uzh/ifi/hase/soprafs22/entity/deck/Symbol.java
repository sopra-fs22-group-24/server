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
    REVERSE("20"),
    HIT_2("20"),
    SKIP("20"),
    DISCARD_ALL("30"),
    WILDCARD("50"),
    EXTREME_HIT("50");

    private static final long serialVersionUID = 1L;

    String stringRepresentation;

    Symbol(String s) {
        stringRepresentation = s;
    }



    public String getString(){
        return stringRepresentation;
    }
    public int getScore() {
        return Integer.parseInt(stringRepresentation);
    }
}


