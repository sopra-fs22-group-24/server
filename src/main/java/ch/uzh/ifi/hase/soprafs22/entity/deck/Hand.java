package ch.uzh.ifi.hase.soprafs22.entity.deck;

import java.io.Serializable;
import java.util.Vector;

public class Hand implements Serializable {

    Vector<Card> hand;

    public Hand() {
        hand = new Vector<>();
    }
    public void addCard(Card card) {
        hand.add(card);
    }

    public boolean containsCard(Card card) {
        return hand.contains(card);
    }


    public void removeCard(Card card) {
        hand.remove(card);
    }
}
