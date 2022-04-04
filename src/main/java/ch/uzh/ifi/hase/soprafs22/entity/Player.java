package ch.uzh.ifi.hase.soprafs22.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Player")
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @OneToOne
    User user;


}
