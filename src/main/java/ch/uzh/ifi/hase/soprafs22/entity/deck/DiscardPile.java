package ch.uzh.ifi.hase.soprafs22.entity.deck;

import java.io.Serializable;
import java.util.Vector;

public class DiscardPile implements Serializable {
    private static final long serialVersionUID = 1L;

    Vector<Card> discardPile;

    //counts the stack of HIT2 Cards the next player has to draw
    int hit2 = 0;

    public int getHit2() {
        return hit2;
    }

    public void increaseHit2(){
        hit2 ++;
    }

    public void resetHit2 (){
        hit2 = 0;
    }

    public DiscardPile() {
        discardPile = new Vector<>();
    }

    public Vector<Card >emptyDiscardPileExceptTopMostCard(){
        Card topMostCard = getTopmostCard();
        discardPile.remove(discardPile.size()-1);
        Vector<Card> cardsToReturn = discardPile;
        discardPile = new Vector<>();
        discardCard(topMostCard);
        return cardsToReturn;
    }
    public void discardCard(Card card) {
        discardPile.add(card);
    }

    public Card getTopmostCard() {
        return discardPile.lastElement();
    }
}
