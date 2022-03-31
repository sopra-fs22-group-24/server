package ch.uzh.ifi.hase.soprafs22.service;

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
    public void createGame() {
        Game game = new Game();


        Deck deck = new Deck();
        game.setDeck(deck);
        /*
        DiscardPile discardPile = new DiscardPile();

        game.setDiscardPile(discardPile);
        */
        //Game game = new Game();
        game.setCard(new Card(Color.BLUE, Symbol.DISCARD_ALL));
        gameRepository.save(game);
        gameRepository.flush();
    }

    /*
    public Vector<Card> drawCards(Game game) {

    }

     */
}
