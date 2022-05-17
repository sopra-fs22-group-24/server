package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.LobbyFullException;
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
    */
    @Test
    public void addUserToLobby_whenLobbyFull_thenThrow() {
        Lobby lobby = new Lobby();
        lobby.setLobbyId(1l);
        lobby.setMaxSize(0);

        User user = new User();

        Mockito.when(lobbyRepository.findByLobbyId(lobby.getLobbyId())).thenReturn(lobby);

        assertThrows(LobbyFullException.class, () -> lobbyService.addUserToLobby(lobby.getLobbyId(), user));
    }
}
