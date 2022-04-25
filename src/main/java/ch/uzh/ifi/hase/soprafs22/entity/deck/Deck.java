package ch.uzh.ifi.hase.soprafs22.entity.deck;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;

public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;

    private Vector<Card> deck;

    public Deck() {
        this.deck = new Vector<Card>();
        // initialize deck
        for(Color color: Color.values() ) {
            for(Symbol symbol: Symbol.values()) {
                deck.add(new Card(color, symbol));
            }
        }
        Collections.shuffle(deck);
    }

    /*
    Shuffles the deck
    adds all discardedCards and then shuffles, such that cards that are
    still in player hands are not reused
     */
    public void shuffle(Vector<Card> discardedCards) {
        Collections.shuffle(discardedCards);
        deck = discardedCards;
    }

    /*
    returns the last card (index wise) from the deck and removes it from the deck
     */
    public Card drawCard() {
        Card card = deck.remove(deck.size()-1);
        return card;
    }



}
