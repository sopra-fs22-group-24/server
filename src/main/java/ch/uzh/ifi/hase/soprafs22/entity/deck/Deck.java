package ch.uzh.ifi.hase.soprafs22.entity.deck;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;
/*
Deck content:
18 Blue cards - 1 to 9
18 Green cards - 1 to 9
18 Red cards - 1 to 9
18 Yellow cards - 1 to 9
8 Hit 2 cards - 2 each in blue, green, red and yellow
8 Reverse cards - 2 each in blue, green, red and yellow
8 Skip cards - 2 each in blue, green, red and yellow
8 Discard All cards - 2 each in blue, green, red and yellow
4 Wild cards
4 Extreme Hit cards
 */
public class Deck implements Serializable {
    private static final long serialVersionUID = 1L;

    private Vector<Card> deck;

    public Deck() {
        // TODO discard all cards 4 each color once
        this.deck = new Vector<>();
        // initialize deck
        for(Color color: Color.values() ) {
            if (color == Color.NULL) {
                continue;
            }
            for(Symbol symbol: Symbol.values()) {
                if (symbol == Symbol.WILDCARD || symbol == Symbol.EXTREME_HIT) {
                    deck.add(new Card(Color.NULL, symbol));
                }
                else {
                    deck.add(new Card(color, symbol));
                    deck.add(new Card(color, symbol));
                }
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
        for (Card card: deck) {
            if(card.getSymbol() == Symbol.WILDCARD || card.getSymbol() == Symbol.EXTREME_HIT) {
                card.setColor(Color.NULL);
            }
        }
    }

    /*
    returns the last card (index wise) from the deck and removes it from the deck
     */
    public Card drawCard() {
        return deck.remove(deck.size()-1);
        
    }
    //to avoid indexOutOfBounds when deck is empty
    public boolean deckIsEmpty(){
        return deck.isEmpty();
    }



}
