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
        for(Card handCard: hand) {
            if (handCard.getSymbol() == card.getSymbol()) {

                if (handCard.getColor() == card.getColor()) {
                    return true;
                }
                if (handCard.getSymbol() == Symbol.WILDCARD || handCard.getSymbol() == Symbol.EXTREME_HIT) {
                    return true;
                }
            }
        }
        return false;
    }


    public void removeCard(Card card) {
        for(int i=0; i<hand.size();i++) {

            Card handCard = hand.get(i);
            if (handCard.getSymbol() == card.getSymbol() && handCard.getColor() == card.getColor()) {
                hand.remove(i);
                return;
            }
        }
    }

    public int getCardCount() {
        return hand.size();
    }

    public Iterable<Card> getCards() {
        return hand;
    }
}
