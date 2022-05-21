package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Deck;
import ch.uzh.ifi.hase.soprafs22.entity.deck.DiscardPile;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.LobbyFullException;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.LobbyNotExistsException;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.UserAlreadyInLobbyException;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;

import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest

public class LobbyServiceIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LobbyRepository lobbyRepository;
    @Autowired
    LobbyService lobbyService;
    @Autowired
    GameRepository gameRepository;
    @Autowired
    GameService gameService;
    User dummyUser;
    @BeforeEach
    public void setup() {
        gameRepository.deleteAll();
        lobbyRepository.deleteAll();
        userRepository.deleteAll();

        dummyUser = createUser(1l);
    }

    public User createUser(long id) {
        User user = new User();
        user.setUsername(String.format("user %d",id));
        user.setPassword(String.format("password %d",id));
        user.setStatus(UserStatus.ONLINE);
        user.setToken(String.format("token %d",id));
        //user.setId(id);
        userRepository.saveAndFlush(user);
        return user;
    }

    @Test
    public void leaveLobby_whenLobbyEmptyAfterLeaving_thenLobbyIsDeleted() {
        Lobby lobby = new Lobby();



        lobby.addUser(dummyUser);
        lobby = lobbyRepository.saveAndFlush(lobby);
        lobbyService.leaveLobby(lobby.getLobbyId(),dummyUser);

        Lobby lobbyAfter = lobbyRepository.findByLobbyId(lobby.getLobbyId());
        assertNull(lobbyAfter);
    }

    @Test
    public void leaveLobby_whenUserLeavesLobby_thenLobbyHasOneUserLess() {
        User u = createUser(2);

        Lobby lobby = new Lobby();
        lobby.addUser(u);
        lobby.addUser(dummyUser);
        lobby.setMaxSize(2);
        lobby = lobbyRepository.saveAndFlush(lobby);

        lobbyService.leaveLobby(lobby.getLobbyId(),dummyUser);

        Lobby lobbyAfter = lobbyRepository.findByLobbyId(lobby.getLobbyId());
        assertEquals(lobbyAfter.getPlayers().size(),1, "invalid player size");
        assertFalse(lobbyAfter.containsUser(dummyUser), "dummy user still in lobby");

    }

    @Test
    public void createLobby_whenUserAlreadyInAnotherLobby_thenRemoveUserFromAnotherLobby() {
        User u = createUser(2);
        Lobby lobby1 = new Lobby();
        lobby1.addUser(u);
        lobby1.addUser(dummyUser);
        lobby1.setMaxSize(2);
        lobby1 = lobbyRepository.saveAndFlush(lobby1);
        long lobby1Id = lobby1.getLobbyId();

        Lobby lobby2 = lobbyService.createLobby(dummyUser, 2);
        Lobby lobby1After = lobbyRepository.findByLobbyId(lobby1Id);

        assertEquals(1,lobby1After.getPlayers().size(), "lobby1 has too many players");
        assertFalse(lobby1After.containsUser(dummyUser), "dummy user still in lobby1");
        assertEquals(1,lobby2.getPlayers().size(), "lobby2 has an invalid number of players");
        assertTrue(lobby2.containsUser(dummyUser), "lobby2 doesn't contain creator");

    }

    @Test
    public void destroyLobby_success() {
        Player p = new Player();
        p.setUser(dummyUser);
        Vector<Player> players = new Vector<>();
        players.add(p);

        Game game = new Game();
        game.setDeck(new Deck());
        game.setDiscardPile(new DiscardPile());
        game.setPlayers(players);
        game = gameRepository.saveAndFlush(game);

        Lobby lobby = new Lobby();
        lobby.addUser(dummyUser);
        lobby.setGame(game);
        lobbyRepository.saveAndFlush(lobby);
        long lobbyId = lobby.getLobbyId();

        lobbyService.destroyLobby(game);
        Lobby lobbyAfter = lobbyRepository.findByLobbyId(lobbyId);
        assertNull(lobbyAfter);

    }

    @Test
    public void joinLobby_whenLobbyDoesNotExist_thenThrow() {
        assertThrows(LobbyNotExistsException.class, () -> lobbyService.joinLobby(dummyUser, 1l));
    }

    @Test
    public void joinLobby_whenTryingToJoinTwice_thenThrow() {
        Lobby lobby = new Lobby();
        lobby.addUser(dummyUser);
        lobby = lobbyRepository.saveAndFlush(lobby);
        long lobbyId = lobby.getLobbyId();

        assertThrows(UserAlreadyInLobbyException.class, () -> lobbyService.joinLobby(dummyUser, lobbyId));
    }

    @Test
    public void joinLobby_whenLobbyFull_thenThrow() {
        Lobby lobby = new Lobby();
        lobby.setMaxSize(0);
        lobby = lobbyRepository.saveAndFlush(lobby);
        long lobbyId = lobby.getLobbyId();

        assertThrows(LobbyFullException.class, () -> lobbyService.joinLobby(dummyUser, lobbyId));
    }

}
