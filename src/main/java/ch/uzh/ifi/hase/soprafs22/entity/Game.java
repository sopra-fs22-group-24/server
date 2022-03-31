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

    /*
    @Column(nullable = false)
    private Vector<User> players;
    */

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

    /*
    public Vector<User> getPlayers() {
        return players;
    }

    public void setPlayers(Vector<User> players) {
        this.players = players;
    }
    */

}
