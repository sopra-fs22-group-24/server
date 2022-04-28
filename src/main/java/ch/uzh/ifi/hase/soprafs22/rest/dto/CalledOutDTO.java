package ch.uzh.ifi.hase.soprafs22.rest.dto;

public class CalledOutDTO {

    private String callee;
    private String calledOutPlayer;

    public String getCalledOutPlayer() {
        return calledOutPlayer;
    }

    public void setCalledOutPlayer(String calledOutPlayer) {
        this.calledOutPlayer = calledOutPlayer;
    }

    public String getCallee() {
        return callee;
    }

    public void setCallee(String callee) {
        this.callee = callee;
    }
}
