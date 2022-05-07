package ch.uzh.ifi.hase.soprafs22.entity.deck;

import java.io.Serializable;

public enum Color implements Serializable {
    RED("Red"),
    BLUE("Blue"),
    GREEN("Green"),
    YELLOW("Yellow"),
    NULL("NULL");
    private static final long serialVersionUID = 1L;

    String stringRepresentation;

    Color(String s){
        stringRepresentation = s;
    }
    public String getString(){
        return stringRepresentation;
    }

}
