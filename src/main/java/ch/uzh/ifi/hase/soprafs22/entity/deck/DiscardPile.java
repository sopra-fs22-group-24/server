package ch.uzh.ifi.hase.soprafs22.entity.deck;

import java.io.Serializable;
import java.util.Vector;

public class DiscardPile implements Serializable {
    private static final long serialVersionUID = 1L;

    Vector<Card> discardPile;

    public DiscardPile() {}

    public void discardCard(Card card) {
        discardPile.add(card);
    }

    public Card getTopmostCard() {
        return discardPile.lastElement();
    }
}
