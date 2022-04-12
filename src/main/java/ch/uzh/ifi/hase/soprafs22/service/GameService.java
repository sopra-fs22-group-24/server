package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.*;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GameService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final GameRepository gameRepository;

    @Autowired
    public GameService(@Qualifier("gameRepository") GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }


    public Game createGame(long lobbyId, User user) {

        // get lobby

        // check if user is creator/admin of lobby

        // transform all users of lobby to players

        // initialise game

        // return game
        /*
        Game game = new Game();


        Deck deck = new Deck();
        game.setDeck(deck);

        DiscardPile discardPile = new DiscardPile();
        game.setDiscardPile(discardPile);

        gameRepository.save(game);
        gameRepository.flush();

         */
        return null;
    }


}
