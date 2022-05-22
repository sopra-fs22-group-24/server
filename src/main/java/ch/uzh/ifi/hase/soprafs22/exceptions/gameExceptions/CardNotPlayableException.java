package ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions;

public class CardNotPlayableException extends GameException {
    public CardNotPlayableException(){
        super("This card is not playable");
    }
}
