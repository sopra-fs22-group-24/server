package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.LobbyFullException;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.LobbyNotExistsException;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
public class LobbyServiceTest {

    @Mock
    LobbyRepository lobbyRepository;
    @Mock
    MessageService messageService;
    @InjectMocks
    LobbyService lobbyService;

    /*
    @Test
    public void removeGameFromLobby_success() {
        Game game = new Game();
        game.setGameId(1l);
        Lobby lobby = new Lobby();
        lobby.setLobbyId(1l);
        lobby.setGame(game);

        Mockito.when(lobbyRepository.findByGame(game)).thenReturn(lobby);

        lobbyService.destroyLobby(game);
        assertNull(lobby);
    }

    @Test
    public void addUserToLobby_whenLobbyFull_thenThrow() {
        Lobby lobby = new Lobby();
        lobby.setLobbyId(1l);
        lobby.setMaxSize(0);

        User user = new User();

        Mockito.when(lobbyRepository.findByLobbyId(lobby.getLobbyId())).thenReturn(lobby);

        assertThrows(LobbyFullException.class, () -> lobbyService.addUserToLobby(lobby.getLobbyId(), user));
    }

    @Test
    public void addUserToLobby_success() {
        Lobby lobby = new Lobby();
        lobby.setLobbyId(1l);
        lobby.setMaxSize(1);

        User user = new User();
        user.setId(1l);

        Mockito.when(lobbyRepository.findByLobbyId(lobby.getLobbyId())).thenReturn(lobby);

        lobbyService.addUserToLobby(lobby.getLobbyId(),user);
        assertEquals(user.getId(), lobby.getPlayers().get(0).getId());
    }

     */
    @Test
    public void leaveLobby_whenLobbyDoesNotExist_thenThrow() {
        Mockito.when(lobbyRepository.findByLobbyId(0l)).thenReturn(null);
        assertThrows(LobbyNotExistsException.class,() -> lobbyService.leaveLobby(0l, new User()));
    }
/*
    @Test
    public void leaveLobby_whenLobbyEmptyAfterLeaving_thenLobbyIsDeleted() {
        Lobby lobby = new Lobby();
        lobby.setLobbyId(1l);

        User user = new User();
        user.setId(1l);

        lobby.addUser(user);

        Mockito.when(lobbyRepository.findByLobbyId(1l)).thenReturn(lobby);
        lobbyService.leaveLobby(lobby.getLobbyId(),user);
        assertNull()
    }
    */

}
