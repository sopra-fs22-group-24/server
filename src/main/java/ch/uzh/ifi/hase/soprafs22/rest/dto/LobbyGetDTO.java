package ch.uzh.ifi.hase.soprafs22.rest.dto;

import ch.uzh.ifi.hase.soprafs22.entity.User;

import java.util.Vector;

public class LobbyGetDTO {

    private Long lobbyId;
    private Vector<User> players;
    private int maxSize;

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public Vector<User> getPlayers() {
        return players;
    }

    public void setPlayers(Vector<User> players) {
        this.players = players;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
