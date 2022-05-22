package ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions;

public class CardNotInHandException extends GameException {
    public CardNotInHandException() {
        super("You don't have the card in your hand");
    }
}
