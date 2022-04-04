package ch.uzh.ifi.hase.soprafs22.entity;



import javax.persistence.*;
import java.util.Vector;

@Entity
@Table(name = "LOBBY")
public class Lobby {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    Long lobbyId;

    @Column(nullable = false)
    Vector<User> players;

    //@Column(nullable = true)
    @OneToOne
    Game game;

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        lobbyId = lobbyId;
    }

    public Vector<User> getPlayers() {
        return players;
    }

    public void setPlayers(Vector<User> players) {
        this.players = players;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean containsUser(User user) {
        for(User u: players) {
            if(u.equals(user)) {
                return true;
            }
        }
        return false;
    }

    public void addUser(User user) {
        players.add(user);
    }

    public void removeUser(User user) {
        players.remove(user);
    }
}
