package ch.uzh.ifi.hase.soprafs22.service;


import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;

import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class LobbyService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final LobbyRepository lobbyRepository;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository) {
        this.lobbyRepository = lobbyRepository;
    }

    public Lobby createLobby(User user) {
        Lobby newLobby = new Lobby();
        newLobby.addUser(user);

        Lobby createdLobby = lobbyRepository.save(newLobby);
        lobbyRepository.flush();
        return createdLobby;
    }

    public void addUserToLobby(Long lobbyId, User user) {
        Lobby lobby = lobbyRepository.findByLobbyId(lobbyId);
        lobby.addUser(user);

        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }

    public List<Lobby> getLobbies() {
        return lobbyRepository.findAll();
    }



    public Lobby joinLobby(User user, long lobbyId) {
        Lobby lobby = lobbyRepository.findByLobbyId(lobbyId);
        if (lobby == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        lobby.addUser(user);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
        return lobby;
    }
}
