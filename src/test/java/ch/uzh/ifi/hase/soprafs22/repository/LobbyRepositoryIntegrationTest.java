package ch.uzh.ifi.hase.soprafs22.repository;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Deck;
import ch.uzh.ifi.hase.soprafs22.entity.deck.DiscardPile;
import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
public class LobbyRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    GameRepository gameRepository;
    @BeforeEach
    public void setup() {
        lobbyRepository.deleteAll();
    }
    @Test
    public void findByGame_success() {
        Game game = new Game();
        //game.setGameId(2l);
        game.setDeck(new Deck());
        game.setDiscardPile(new DiscardPile());
        game = gameRepository.saveAndFlush(game);
        Lobby lobby = new Lobby();
        //lobby.setLobbyId(1l);
        lobby.setGame(game);
        lobby = lobbyRepository.saveAndFlush(lobby);
        Lobby lobby2 = lobbyRepository.findByGame(game);
        assertEquals(lobby.getLobbyId(), lobby2.getLobbyId());
    }

}
