package ch.uzh.ifi.hase.soprafs22.entity;

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
    boolean unoIsCalled=false;

    @Column()
    boolean reverse=false;

    public boolean isUnoIsCalled() {
        return unoIsCalled;
    }

    public void setUnoIsCalled(boolean unoIsCalled) {
        this.unoIsCalled = unoIsCalled;
    }
    public Game() {
        this.players = new Vector<>();
    }
    public Long getGameId() {
        return gameId;
    }
    public void reverseTurndirection(){
        reverse=!reverse;
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
        long userId = user.getId();
        for(Player player: players) {
            // The following does not work somehow
            // player.getUser().getId() == user.getId()
            // Therefore cast it to variables
            long playerId = player.getUser().getId();
            if (playerId == userId) {
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
        return turnPlayer.getUser().getId().equals( player.getUser().getId());
    }

    public Player getPlayerTurn() {
        return players.get(turnIndex);
    }

    public Player getNextPlayer() {
        int turnIndexCopy = turnIndex;
        if(reverse) {
            turnIndexCopy--;
        } else {
            turnIndexCopy++;
        }
        if(turnIndexCopy < 0) {
            turnIndexCopy = players.size()-1;
        }
        if(turnIndexCopy >= players.size()) {
            turnIndexCopy = 0;
        }

        return players.get(turnIndexCopy);
    }
}
