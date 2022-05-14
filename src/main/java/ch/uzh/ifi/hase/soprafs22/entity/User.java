package ch.uzh.ifi.hase.soprafs22.entity;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Comparator;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */


@Entity
@Table(name = "USER")
public class User implements Serializable, Comparable<User> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long id;
    //Lea: password und name ausgetauscht
    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @Column()
    private int gamesPlayed;

    @Column()
    private int gamesWon;

    @Column()
    private int score;

    @Column()
    private String principalName;


    @Column()
    private String picture;

    public String getPicture() {
        return picture;
    }

    public void setPicture(String profilpicture) {
        this.picture = profilpicture;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //Lea: password und name ausgetauscht
    public String getPassword() {
        return password;
    }

    //Lea: password und name ausgetauscht
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public int getGamesPlayed() { return gamesPlayed; }

    public void setGamesPlayed(int gamesPlayed) {this.gamesPlayed = gamesPlayed; }

    public int getGamesWon() {return gamesWon;}

    public void setGamesWon(int gamesWon) {this.gamesWon = gamesWon; }

    public int getScore() { return score; }

    public void setScore(int points) { this.score = this.score + points; }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    @Override
    public int compareTo(User o) {
        return score - o.getScore();
    }
}
