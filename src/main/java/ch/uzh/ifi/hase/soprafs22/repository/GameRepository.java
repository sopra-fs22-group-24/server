package ch.uzh.ifi.hase.soprafs22.repository;

import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("gameRepository")
public interface GameRepository extends JpaRepository<Game, Long> {
    public Game findByGameId(long gameId);


    Game findByPlayers(User user);
}
