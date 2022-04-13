package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.*;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.*;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class GameServiceTest {
    @Mock
    GameRepository gameRepository;

    @Mock
    MessageService messageService;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setup() {
    }

    @Test
    public void playCard_whenGameIdInvalid_thenThrowException() {
        System.out.println(gameService);
        User user = new User();
        long gameId = 128;
        Card card = new Card();

        assertThrows(GameNotExistsException.class,() -> gameService.playCard(gameId, user, card));
    }

    @Test
    public void playCard_whenPlayerNotInGame_thenThrowException() {
        Game game = new Game();

        long gameId = 128;
        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        User user = new User();
        Card card = new Card();
        //gameService.playCard(gameId, user, card);
        assertThrows(PlayerNotInGameException.class, () -> gameService.playCard(gameId, user, card));
    }

    @Test
    public void playCard_whenNotPlayerTurn_thenThrowException() {
        Game game = new Game();
        long gameId = 128;
        game.setGameId(gameId);
        User user1 = new User();
        User user2 = new User();
        user1.setId(1l);
        user2.setId(2l);
        Player player1 = new Player();
        Player player2 = new Player();
        Vector<Player> players = new Vector<>();
        player1.setUser(user1);
        player2.setUser(user2);
        players.add(player1);
        players.add(player2);
        game.setPlayers(players);


        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        Card card = new Card();
        //gameService.playCard(gameId, user, card);
        assertThrows(NotPlayerTurnException.class, () -> gameService.playCard(gameId, user2, card));
    }

    @Test
    public void playCard_whenCardNotInPlayerHand_thenThrowException() {
        Game game = new Game();
        long gameId = 128;
        game.setGameId(gameId);
        User user = new User();
        user.setId(1l);

        Player player = new Player();
        player.setUser(user);

        Hand hand = new Hand();
        hand.addCard(new Card(Color.BLUE, Symbol.DISCARD_ALL));

        player.setHand(hand);

        Vector<Player> players = new Vector<>();
        players.add(player);

        game.setPlayers(players);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        //gameService.playCard(gameId, user, card);
        assertThrows(CardNotInHandException.class, () -> gameService.playCard(gameId, user, card));
    }

    @Test
    public void playCard_whenCardColorNotSameAndRegularCard_thenThrowException() {
        Game game = new Game();
        long gameId = 128;
        game.setGameId(gameId);
        User user = new User();
        user.setId(1l);

        Player player = new Player();
        player.setUser(user);

        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);

        player.setHand(hand);

        Vector<Player> players = new Vector<>();
        players.add(player);

        game.setPlayers(players);

        DiscardPile pile = new DiscardPile();
        pile.discardCard(new Card(Color.RED, Symbol.DISCARD_ALL));

        game.setDiscardPile(pile);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        //gameService.playCard(gameId, user, card);
        assertThrows(CardNotPlayableException.class, () -> gameService.playCard(gameId, user, card));
    }

    @Test
    public void playCard_whenCardPlayable_then_removeCardFromPlayerHand_addCardToDiscardPile_IncreaseTurnOrderByOne() {
        Game game = new Game();
        long gameId = 128;
        game.setGameId(gameId);
        User user1 = new User();
        user1.setId(1l);

        User user2 = new User();
        user2.setId(2l);

        Player player1 = new Player();
        player1.setUser(user1);

        Player player2 = new Player();
        player2.setUser(user2);

        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);

        player1.setHand(hand);

        Vector<Player> players = new Vector<>();
        players.add(player1);
        players.add(player2);

        game.setPlayers(players);
        DiscardPile pile = new DiscardPile();
        pile.discardCard(new Card(Color.BLUE, Symbol.DISCARD_ALL));

        game.setDiscardPile(pile);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        gameService.playCard(gameId, user1, card);

        assertFalse(player1.getHand().containsCard(card), "card still in player hand");
        Card topMostCard = pile.getTopmostCard();
        assertEquals(topMostCard.getColor(), card.getColor(), "Color not the same");
        assertEquals(topMostCard.getSymbol(), card.getSymbol(), "Symbol not the same");

        assertTrue(game.checkPlayerTurn(player2), "Turn order not increased");
    }
}