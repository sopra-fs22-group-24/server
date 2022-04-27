package ch.uzh.ifi.hase.soprafs22.rest.dto;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;

public class PlayCardDTO {
    private Card card;
    private boolean uno;
    private User user;

    public Card getCard() {
        return card;
    }

    public void setUno(boolean uno) {
        this.uno = uno;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public boolean getUno() {
        return uno;
    }

    public User getUser() {
        return user;
    }
}
