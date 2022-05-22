package ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions;

public class CardColorNotChoosenException extends GameException{
    public CardColorNotChoosenException(){
        super("Please choose a color");
    }
}
