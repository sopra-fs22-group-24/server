package ch.uzh.ifi.hase.soprafs22.entity;

import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Deck;
import ch.uzh.ifi.hase.soprafs22.entity.deck.DiscardPile;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Vector;

@Entity
@Table(name = "GAME")
public class Game implements Serializable  {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long gameId;

    @Column(nullable = false, length = 2048)
    private Deck deck;

    @Column(nullable = false, length = 2048)
    private DiscardPile discardPile;

    @Column(nullable = false, length = 32768)
    Vector<Player> players;

    @Column()
    int turnIndex;

    @Column()
    boolean reverse;
    /*
    @Column(nullable = false)
    private Vector<User> players;
    */
    public Game() {
        this.players = new Vector<>();
    }
    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }


    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public DiscardPile getDiscardPile() {
        return discardPile;
    }

    public void setDiscardPile(DiscardPile discardPile) {
        this.discardPile = discardPile;
    }


    public Vector<Player> getPlayers() {
        return players;
    }

    public void setPlayers(Vector<Player> players) {
        this.players = players;
    }





    public Player getPlayerFromUser(User user) {
        for(Player player: players) {
            System.out.println(player.getUser().getId());
            System.out.println(user.getId());
            System.out.println(user.getId() == player.getUser().getId());
            if (player.getUser().getId() == user.getId()) {
                return player;
            }
        }
        return null;
    }

    public void nextTurn() {
        if(reverse) {
            turnIndex--;
        } else {
            turnIndex++;
        }
        if(turnIndex < 0) {
            turnIndex = players.size()-1;
        }
        if(turnIndex >= players.size()) {
            turnIndex = 0;
        }

    }

    public boolean checkPlayerTurn(Player player) {
        Player turnPlayer = players.get(turnIndex);
        return turnPlayer.getUser().getId() == player.getUser().getId();
    }
}
