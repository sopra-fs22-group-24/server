package ch.uzh.ifi.hase.soprafs22.rest.dto;

import ch.uzh.ifi.hase.soprafs22.entity.User;

import java.util.Vector;

public class LobbyPostDTO {
    private Long lobbyId;
    private Vector<User> players;

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyID) {
        this.lobbyId = lobbyID;
    }

    public Vector<User> getPlayers() {
        return players;
    }

    public void setPlayers(Vector<User> players) {
        this.players = players;
    }
}
