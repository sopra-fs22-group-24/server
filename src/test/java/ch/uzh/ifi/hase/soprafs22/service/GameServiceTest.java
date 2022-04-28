package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.*;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.*;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.utils.Globals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.Lob;
import java.util.Random;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class GameServiceTest {
    @Mock
    GameRepository gameRepository;


    @Mock
    MessageService messageService;

    @Mock
    Random random;

    @Mock
    LobbyService lobbyService;

    @Mock
    Globals globals;
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

        assertThrows(GameNotExistsException.class,() -> gameService.playCard(gameId, user, card, null, false));
    }

    @Test
    public void playCard_whenPlayerNotInGame_thenThrowException() {
        Game game = new Game();

        long gameId = 128;
        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        User user = new User();
        user.setId(1l);
        Card card = new Card();
        //gameService.playCard(gameId, user, card);
        assertThrows(PlayerNotInGameException.class, () -> gameService.playCard(gameId, user, card, null, false));
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
        assertThrows(NotPlayerTurnException.class, () -> gameService.playCard(gameId, user2, card, null, false));
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
        assertThrows(CardNotInHandException.class, () -> gameService.playCard(gameId, user, card, null, false));
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
        assertThrows(CardNotPlayableException.class, () -> gameService.playCard(gameId, user, card, null, false));
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
        Card card2 = new Card(Color.RED, Symbol.EIGHT);

        Hand hand = new Hand();
        hand.addCard(card);
        hand.addCard(card2);

        player1.setHand(hand);

        Vector<Player> players = new Vector<>();
        players.add(player1);
        players.add(player2);

        game.setPlayers(players);
        DiscardPile pile = new DiscardPile();
        pile.discardCard(new Card(Color.BLUE, Symbol.DISCARD_ALL));

        game.setDiscardPile(pile);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        gameService.playCard(gameId, user1, card, null, false);

        assertFalse(player1.getHand().containsCard(card), "card still in player hand");
        assertEquals( 1, player1.getHand().getCardCount(), "card count is wrong");
        Card topMostCard = pile.getTopmostCard();
        assertEquals(topMostCard.getColor(), card.getColor(), "Color not the same");
        assertEquals(topMostCard.getSymbol(), card.getSymbol(), "Symbol not the same");

        assertTrue(game.checkPlayerTurn(player2), "Turn order not increased");
    }

    @Test
    public void startGame_whenPlayerNotAdmin_thenThrowException() {
        //Atm the user with index 0 in the lobby is the admin

        User u1 = new User();
        User u2 = new User();

        u1.setId(1l);
        u2.setId(2l);

        long lobbyId = 1;
        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbyId);
        lobby.addUser(u1);
        lobby.addUser(u2);


        Mockito.when(lobbyService.findByLobbyId(lobbyId)).thenReturn(lobby);

        assertThrows(UserNotLobbyAdminException.class,() -> gameService.createGame(lobbyId,u2));

    }

    @Test
    public void startGame_whenSuccessful_thenEachPlayerInGameAndHasHand() {
        //Atm the user with index 0 in the lobby is the admin

        User u1 = new User();
        User u2 = new User();

        u1.setId(1l);
        u2.setId(2l);

        long lobbyId = 1;
        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbyId);
        lobby.addUser(u1);
        lobby.addUser(u2);


        Mockito.when(lobbyService.findByLobbyId(lobbyId)).thenReturn(lobby);
        Mockito.when(gameRepository.save(Mockito.any())).then(AdditionalAnswers.returnsFirstArg());
        Game game = gameService.createGame(lobbyId,u1);
        assertEquals(u1.getId(),game.getPlayers().get(0).getUser().getId());
        assertEquals(u2.getId(),game.getPlayers().get(1).getUser().getId());
        assertEquals(7, game.getPlayers().get(0).getHand().getCardCount());
        assertEquals(7, game.getPlayers().get(1).getHand().getCardCount());

    }

    @Test
    public void playCard_whenSkip_thenIncreaseTurnByTwo() {
        Game game = new Game();
        long gameId = 128;
        game.setGameId(gameId);
        User user1 = new User();
        user1.setId(1l);

        User user2 = new User();
        user2.setId(2l);

        User user3 = new User();
        user3.setId(3l);

        Player player1 = new Player();
        player1.setUser(user1);

        Player player2 = new Player();
        player2.setUser(user2);

        Player player3 = new Player();
        player3.setUser(user3);



        Card card = new Card(Color.BLUE, Symbol.SKIP);
        Card card2 = new Card(Color.RED, Symbol.EIGHT);

        Hand hand = new Hand();
        hand.addCard(card);
        hand.addCard(card2);

        player1.setHand(hand);

        Vector<Player> players = new Vector<>();
        players.add(player1);
        players.add(player2);
        players.add(player3);

        game.setPlayers(players);
        DiscardPile pile = new DiscardPile();
        pile.discardCard(new Card(Color.BLUE, Symbol.DISCARD_ALL));

        game.setDiscardPile(pile);




        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        gameService.playCard(gameId, user1, card, null, false);

        assertFalse(player1.getHand().containsCard(card), "card still in player hand");
        assertEquals( 1, player1.getHand().getCardCount(), "card count is wrong");
        Card topMostCard = pile.getTopmostCard();
        assertEquals(topMostCard.getColor(), card.getColor(), "Color not the same");
        assertEquals(topMostCard.getSymbol(), card.getSymbol(), "Symbol not the same");

        assertTrue(game.checkPlayerTurn(player3), "Turn order not increased");
    }

    @Test
    public void playCard_whenHit2_thenNextPlayerHasToDraw() {
        /*
        WARNING FLAKY TEST
        In this test we test a function that uses randomness, because of this there is the possibility that a player
        doesn't have to draw a card and this test will fail.
        If this test fails retry and hopefully it will pass.
         */
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

        Card card = new Card(Color.BLUE, Symbol.HIT_2);
        Card card2 = new Card(Color.RED, Symbol.EIGHT);

        Hand hand = new Hand();
        hand.addCard(card);
        hand.addCard(card2);

        player1.setHand(hand);
        Hand hand2 = new Hand();
        player2.setHand(hand2);

        Vector<Player> players = new Vector<>();
        players.add(player1);
        players.add(player2);

        game.setPlayers(players);
        DiscardPile pile = new DiscardPile();
        pile.discardCard(new Card(Color.BLUE, Symbol.DISCARD_ALL));
        game.setDiscardPile(pile);

        Deck deck = new Deck();
        game.setDeck(deck);
        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);
        Mockito.when(random.nextInt(12)).thenReturn(1);
        gameService.playCard(gameId, user1, card, null, false);



        assertFalse(player1.getHand().containsCard(card), "card still in player hand");
        assertEquals( 1, player1.getHand().getCardCount(), "card count is wrong");
        Card topMostCard = pile.getTopmostCard();
        assertEquals(topMostCard.getColor(), card.getColor(), "Color not the same");
        assertEquals(topMostCard.getSymbol(), card.getSymbol(), "Symbol not the same");
        assertEquals(2, player2.getHand().getCardCount(), "player 2 didn't draw");

    }

    @Test
    public void playCard_WhenWildcard_thenSetColorCorrectly() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        Card card = new Card(Color.BLUE, Symbol.WILDCARD);
        Hand hand = new Hand();
        hand.addCard(card);

        User u = new User();
        u.setId(1l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);
        Vector<Player> players = new Vector<>();
        players.add(p);
        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        gameService.playCard(gameId, u, card,null, false);

        Card topMostCard = d.getTopmostCard();
        assertEquals(card.getColor(),topMostCard.getColor());
        assertEquals(card.getSymbol(),topMostCard.getSymbol());
    }

    @Test
    public void playCard_whenWildCardColorNotChosen_thenThrow() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        Card card = new Card(null, Symbol.WILDCARD);
        Hand hand = new Hand();
        hand.addCard(card);

        User u = new User();
        u.setId(1l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);
        Vector<Player> players = new Vector<>();
        players.add(p);
        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        assertThrows(CardColorNotChoosenException.class, () -> gameService.playCard(gameId, u, card,null, false));

    }

    @Test
    public void playCard_whenExtremeHitPlayed_ColorIsSetAndVictimHasDrawnCards() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EXTREME_HIT);
        Hand hand = new Hand();
        hand.addCard(card);

        Hand hand2 = new Hand();

        User u = new User();
        u.setId(1l);

        User u2 = new User();
        u2.setId(2l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);
        Mockito.when(random.nextInt(12)).thenReturn(1);

        gameService.playCard(gameId, u, card,u2, false);

        Card topMostCard = d.getTopmostCard();
        assertEquals(card.getColor(), topMostCard.getColor(), "Color not correct");
        assertEquals(card.getSymbol(), topMostCard.getSymbol(), "Symbol not correct");
        assertEquals(1, p2.getHand().getCardCount());
    }

    @Test
    public void playCard_whenExtremeHitPlayedButNoColor_thenThrow() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        Deck deck = new Deck();
        Card card = new Card(null, Symbol.EXTREME_HIT);
        Hand hand = new Hand();
        hand.addCard(card);

        Hand hand2 = new Hand();

        User u = new User();
        u.setId(1l);

        User u2 = new User();
        u2.setId(2l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        assertThrows(CardColorNotChoosenException.class, () -> gameService.playCard(gameId, u, card, u2, false));
    }

    @Test
    public void playCard_whenExtremeHitPlayedButInvalidVictim_thenThrow() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        Deck deck = new Deck();
        Card card = new Card(null, Symbol.EXTREME_HIT);
        Hand hand = new Hand();
        hand.addCard(card);

        Hand hand2 = new Hand();

        User u = new User();
        u.setId(1l);

        User u2 = new User();
        u2.setId(2l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        assertThrows(CardColorNotChoosenException.class, () -> gameService.playCard(gameId, u, card, null, false));
    }
}