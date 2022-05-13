package ch.uzh.ifi.hase.soprafs22.entity;



import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Entity
@Table(name = "LOBBY")
public class Lobby {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    Long lobbyId;

    //@Column(nullable = false, length = 32768)
    @ManyToMany(targetEntity = User.class, fetch = FetchType.EAGER)
    List<User> players;

    //@Column(nullable = true)
    @OneToOne
    Game game;

    public Lobby() {
        players = new ArrayList<>();
    }

    public Long getLobbyId() {
        return lobbyId;
    }

    public void setLobbyId(Long lobbyId) {
        this.lobbyId = lobbyId;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void setPlayers(List<User> players) {
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
            if(u.getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    public void addUser(User user) {
        players.add(user);
    }

    public void removeUser(User target) {
        User toDelete = null;
        for(User user : players) {
            if(user.getId().equals(target.getId())) {
                toDelete = user;
                break;

            }
        }
        players.remove(toDelete);

    }

    public boolean userIsAdmin(User user) {
        User admin = players.get(0);
        long id1 = user.getId();
        long id2 = admin.getId();
        return id1==id2;
    }
}
