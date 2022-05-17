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

    @Mock
    UserService userService;
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

        assertThrows(GameNotExistsException.class, () -> gameService.playCard(gameId, user, card, null, false));
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
        user2.setPrincipalName("x");
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
        user.setPrincipalName("x");
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
        user.setPrincipalName("x");
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
        user1.setPrincipalName("x");

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
        player2.setHand(new Hand());
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
        assertEquals(1, player1.getHand().getCardCount(), "card count is wrong");
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

        assertThrows(UserNotLobbyAdminException.class, () -> gameService.createGame(lobbyId, u2));

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
        Game game = gameService.createGame(lobbyId, u1);
        assertEquals(u1.getId(), game.getPlayers().get(0).getUser().getId());
        assertEquals(u2.getId(), game.getPlayers().get(1).getUser().getId());
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
        user1.setPrincipalName("x");

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
        player2.setHand(new Hand());
        player3.setHand(new Hand());
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
        assertEquals(1, player1.getHand().getCardCount(), "card count is wrong");
        Card topMostCard = pile.getTopmostCard();
        assertEquals(topMostCard.getColor(), card.getColor(), "Color not the same");
        assertEquals(topMostCard.getSymbol(), card.getSymbol(), "Symbol not the same");

        assertTrue(game.checkPlayerTurn(player3), "Turn order not increased");
    }

    @Test
    public void playCard_whenHit2_thenNextPlayerHasToDraw() {

        Game game = new Game();
        long gameId = 128;
        game.setGameId(gameId);
        User user1 = new User();
        user1.setId(1l);
        user1.setPrincipalName("x");

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
        Mockito.when(random.nextInt(Mockito.anyInt())).thenReturn(2);
        gameService.playCard(gameId, user1, card, null, false);


        assertFalse(player1.getHand().containsCard(card), "card still in player hand");
        assertEquals(1, player1.getHand().getCardCount(), "card count is wrong");
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
        u.setPrincipalName("x");

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

        gameService.playCard(gameId, u, card, null, false);

        Card topMostCard = d.getTopmostCard();
        assertEquals(card.getColor(), topMostCard.getColor());
        assertEquals(card.getSymbol(), topMostCard.getSymbol());
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
        u.setPrincipalName("x");
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

        assertThrows(CardColorNotChoosenException.class, () -> gameService.playCard(gameId, u, card, null, false));

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
        u.setPrincipalName("x");

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
        Mockito.when(random.nextInt(Mockito.anyInt())).thenReturn(2);

        gameService.playCard(gameId, u, card, u2, false);

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
        u.setPrincipalName("x");
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
        Card card = new Card(Color.BLUE, Symbol.EXTREME_HIT);
        Hand hand = new Hand();
        hand.addCard(card);

        Hand hand2 = new Hand();

        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");
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

        assertThrows(PlayerNotInGameException.class, () -> gameService.playCard(gameId, u, card, null, false));
    }

    @Test
    public void playCard_whenDiscardAll_thenRemoveCardsOfSameColor() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.DISCARD_ALL);
        Hand hand = new Hand();
        hand.addCard(card);
        hand.addCard(new Card(Color.BLUE, Symbol.EIGHT));
        hand.addCard(new Card(Color.BLUE, Symbol.FOUR));
        hand.addCard(new Card(Color.RED, Symbol.FOUR));

        Hand hand2 = new Hand();

        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

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

        gameService.playCard(gameId, u, card, null, false);
        assertEquals(1, p.getHand().getCardCount());

    }

    @Test
    public void playCard_whenDiscardAllWouldDiscardAll_thenOnlyRemoveDiscardAll() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.DISCARD_ALL);
        Hand hand = new Hand();
        hand.addCard(card);
        hand.addCard(new Card(Color.BLUE, Symbol.EIGHT));
        hand.addCard(new Card(Color.BLUE, Symbol.FOUR));

        Hand hand2 = new Hand();

        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

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

        gameService.playCard(gameId, u, card, null, false);
        assertEquals(2, p.getHand().getCardCount());

    }

    @Test
    public void playCard_whenReverse_thenReverseGameTurn() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.REVERSE);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

        User u2 = new User();
        u2.setId(2l);

        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        gameService.playCard(gameId, u, card, null, false);
        assertEquals(p3.getUser().getId(), game.getPlayerTurn().getUser().getId());
    }

    @Test
    public void playCard_whenPlayerSaysUnoAndApplicable_thenUnoIsSaid() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.REVERSE);
        Hand hand = new Hand();
        hand.addCard(card);
        hand.addCard(card);


        Hand hand2 = new Hand();
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

        User u2 = new User();
        u2.setId(2l);

        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        gameService.playCard(gameId, u, card, null, true);
        assertTrue(p.isHasSaidUno());
    }

    @Test
    public void playCard_whenPlayerSaysUnoAndIsNotApplicable_thenUnoIsSaidIsFalse() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.REVERSE);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

        User u2 = new User();
        u2.setId(2l);

        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        gameService.playCard(gameId, u, card, null, true);
        assertFalse(p.isHasSaidUno());
    }

    @Test
    public void playCard_ifPlayerBeforeForgotToSayUnoButWasNotCalledOut_setUnoAfterNextPlayerPlayedCard() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        hand2.addCard(card);
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

        User u2 = new User();
        u2.setId(2l);
        u2.setPrincipalName("x");
        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        //forget to say uno
        gameService.playCard(gameId, u, card, null, false);
        //next player plays
        gameService.playCard(gameId, u2, card, null, false);

        assertTrue(p.isHasSaidUno());
    }

    @Test
    public void drawCard_ifPlayerBeforeForgotToSayUnoButWasNotCalledOut_setUnoAfterNextPlayerPlayedCard() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        hand2.addCard(card);
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

        User u2 = new User();
        u2.setId(2l);
        u2.setPrincipalName("x");
        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);

        //forget to say uno
        gameService.playCard(gameId, u, card, null, false);
        //next player plays
        gameService.drawCard(gameId, u2);

        assertTrue(p.isHasSaidUno());
    }

    @Test
    public void drawCard_whenCalled_thenPlayerDrawsCards() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        hand2.addCard(card);
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

        User u2 = new User();
        u2.setId(2l);

        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);
        Mockito.when(random.nextInt(Mockito.anyInt())).thenReturn(2);
        //forget to say uno
        gameService.drawCard(gameId, u);

        assertEquals(2, p.getHand().getCardCount(), "invalid amount of cards drawn");
        assertTrue(p2.getUser().getId().equals(game.getPlayerTurn().getUser().getId()));
    }

    @Test
    public void drawCard_whenNotPlayerTurn_thenThrow() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        hand2.addCard(card);
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);

        User u2 = new User();
        u2.setId(2l);
        u2.setPrincipalName("x");

        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);
        Mockito.when(random.nextInt(12)).thenReturn(1);
        //forget to say uno
        assertThrows(NotPlayerTurnException.class, () -> gameService.drawCard(gameId, u2));


    }

    @Test
    public void callOutPlayer_whenCalledOutPlayerSaidUno_thenThrow() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        hand2.addCard(card);
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);

        User u2 = new User();
        u2.setId(2l);
        u2.setPrincipalName("x");
        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);
        p.setHasSaidUno(true);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);
        assertThrows(InvalidCallOutException.class, () -> gameService.callOutPlayer(gameId, u2, u));


    }

    @Test
    public void callOutPlayer_whenCalledOutHasMoreThenOneCard_thenThrow() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);
        hand.addCard(card);


        Hand hand2 = new Hand();
        hand2.addCard(card);
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);

        User u2 = new User();
        u2.setId(2l);
        u2.setPrincipalName("x");
        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);


        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);

        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);
        assertThrows(InvalidCallOutException.class, () -> gameService.callOutPlayer(gameId, u2, u));


    }

    @Test
    public void callOutPlayer_whenCalledOutPlayerForgotToSayUno_thenCalledOutPlayerHastoDraw() {
        long gameId = 128l;

        DiscardPile d = new DiscardPile();
        d.discardCard(new Card(Color.BLUE, Symbol.FOUR));
        Deck deck = new Deck();
        Card card = new Card(Color.BLUE, Symbol.EIGHT);
        Hand hand = new Hand();
        hand.addCard(card);


        Hand hand2 = new Hand();
        hand2.addCard(card);
        Hand hand3 = new Hand();
        User u = new User();
        u.setId(1l);
        u.setPrincipalName("x");

        User u2 = new User();
        u2.setId(2l);
        u2.setPrincipalName("x");
        User u3 = new User();
        u3.setId(3l);

        Player p = new Player();
        p.setUser(u);
        p.setHand(hand);

        Player p2 = new Player();
        p2.setUser(u2);
        p2.setHand(hand2);

        Player p3 = new Player();
        p3.setUser(u3);
        p3.setHand(hand3);

        Vector<Player> players = new Vector<>();
        players.add(p);
        players.add(p2);
        players.add(p3);

        Game game = new Game();
        game.setGameId(gameId);
        game.setPlayers(players);
        game.setDiscardPile(d);
        game.setDeck(deck);
        System.out.println(p.getHand().getCardCount());
        Mockito.when(gameRepository.findByGameId(gameId)).thenReturn(game);
        Mockito.when(random.nextInt(Mockito.anyInt())).thenReturn(2);
        gameService.callOutPlayer(gameId, u2, u);

        assertEquals(3, p.getHand().getCardCount());

    }

    @Test
    public void initializeTest() {
        //setup before test
        Long id = 12L;
        User user = new User();
        user.setId(12L);
        user.setPrincipalName("x");

        Game game = new Game();
        //add players
        Vector<Player> players = new Vector<>();
        int k=0;
        for (int i = 0; i<4;++i){
            Player playertoadd = new Player();
            if(k==0){
                playertoadd.setUser(user);
                k=2;
            }
            else {
                playertoadd.setUser(new User());
            }
            players.add(playertoadd);
        }
        //create deck
        Deck deck = new Deck();
        //add Hand to Player with
        for(Player playerToAddCards: players) {
            Hand hand = new Hand();
            for(int i=0; i<7; i++) {
                hand.addCard(deck.drawCard());
            }
            playerToAddCards.setHand(hand);
        }
        DiscardPile discardPile = new DiscardPile();
        discardPile.discardCard(deck.drawCard());
        game.setDeck(deck);
        game.setDiscardPile(discardPile);
        game.setPlayers(players);
        game.setGameId(1L);
        // TODO spy messageservice and verify invokations otherwise this test doesnt make sense :D
       // Mockito.spy(messageService)
        //mock repo
        Mockito.when(gameRepository.findByGameId(id)).thenReturn(game);

        //call function with arguments needed
        gameService.initialize(id,user);
        // assert everyone has 7 cards
        assertEquals(4 , game.getPlayers().size());
        for(int i=0; i<4;++i){
           Player playertoTest = game.getPlayers().get(i);
          assertEquals(7 , playertoTest.getHand().getCardCount() );
        }
    }

    @Test
    public void drawCard_sucess() {
    }

    @Test
    public void drawCard_Throws_gameNotExistsException() {
    }

    @Test
    public void drawCard_Throws_playerInGameException() {
    }
}
