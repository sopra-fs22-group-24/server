package ch.uzh.ifi.hase.soprafs22.service;


import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;

import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.UserAlreadyInLobbyException;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.LobbyNotExistsException;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LobbyService {
    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    private final LobbyRepository lobbyRepository;
    private final MessageService messageService;

    @Autowired
    public LobbyService(@Qualifier("lobbyRepository") LobbyRepository lobbyRepository, MessageService messageService) {
        this.lobbyRepository = lobbyRepository;
        this.messageService = messageService;
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
            throw new LobbyNotExistsException();
        }
        //check if user already in lobby
        if(lobby.containsUser(user)) {
            throw new UserAlreadyInLobbyException();
        }
        lobby.addUser(user);
        lobbyRepository.save(lobby);
        lobbyRepository.flush();
        return lobby;
    }



    public Lobby findByLobbyId(long lobbyId) {
        return lobbyRepository.findByLobbyId(lobbyId);
    }

    public void leaveLobby(long lobbyId,User user) {
        Lobby lobby = lobbyRepository.findByLobbyId(lobbyId);
        if(lobby == null) {
            throw new LobbyNotExistsException();
        }
        lobby.removeUser(user);

        if(lobby.getPlayers().isEmpty()) {
            lobbyRepository.delete(lobby);

        } else {
            lobbyRepository.saveAndFlush(lobby);
        }
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        messageService.sendToLobby(lobby.getLobbyId(),"userLeft", userGetDTO);
    }
}
