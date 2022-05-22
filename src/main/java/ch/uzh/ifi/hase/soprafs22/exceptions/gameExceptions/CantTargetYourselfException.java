package ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions;

public class CantTargetYourselfException extends GameException {
    public CantTargetYourselfException() {
        super("You can't target yourself");
    }
}
