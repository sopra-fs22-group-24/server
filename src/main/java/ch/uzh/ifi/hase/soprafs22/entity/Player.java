package ch.uzh.ifi.hase.soprafs22.entity;

import ch.uzh.ifi.hase.soprafs22.entity.deck.Hand;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Player")
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @OneToOne
    User user;

    @Column()
    Hand hand;

    @Column
    boolean hasSaidUno;

    @Column
    int score;
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
    }

    public void setHasSaidUno(boolean hasSaidUno) {
        this.hasSaidUno = hasSaidUno;
    }

    public boolean isHasSaidUno() {
        return hasSaidUno;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }
}
