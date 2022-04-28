package ch.uzh.ifi.hase.soprafs22.repository;

import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("cardRepository")
public interface CardRepository extends JpaRepository<Card, Long> {
    public Card findById(long id);



}


