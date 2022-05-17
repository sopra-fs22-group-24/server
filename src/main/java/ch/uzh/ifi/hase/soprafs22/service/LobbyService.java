package ch.uzh.ifi.hase.soprafs22.service;


import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;

import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.LobbyFullException;
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

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class LobbyService {
    private final Logger log = LoggerFactory.getLogger(LobbyService.class);

    private final LobbyRepository lobbyRepository;
    private final MessageService messageService;

    @Autowired
    public LobbyService(LobbyRepository lobbyRepository, MessageService messageService) {
        this.lobbyRepository = lobbyRepository;
        this.messageService = messageService;
    }

    public Lobby createLobby(User user, int maxSize) {
        Lobby newLobby = new Lobby();
        newLobby.addUser(user);
        newLobby.setMaxSize(maxSize);
        handlePlayerAlreadyInOtherLobby(user);
        Lobby createdLobby = lobbyRepository.save(newLobby);
        lobbyRepository.flush();
        return createdLobby;
    }

    public void addUserToLobby(Long lobbyId, User user) {
        Lobby lobby = lobbyRepository.findByLobbyId(lobbyId);
        if(lobby.getMaxSize()<=lobby.getPlayers().size()+1) {
            throw new LobbyFullException();
        }
        handlePlayerAlreadyInOtherLobby(user);
        Lobby otherLobby = lobbyRepository.findByPlayersContaining(user);
        if(otherLobby != null) {
            otherLobby.removeUser(user);
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
            lobbyRepository.saveAndFlush(otherLobby);
            messageService.sendToLobby(otherLobby.getLobbyId(), "userLeft", userGetDTO);
        }
        lobby.addUser(user);

        lobbyRepository.save(lobby);
        lobbyRepository.flush();
    }

    private void handlePlayerAlreadyInOtherLobby(User user) {
        Lobby otherLobby = lobbyRepository.findByPlayersContaining(user);
        if(otherLobby != null) {
            otherLobby.removeUser(user);
            if(otherLobby.getPlayers().isEmpty()) {
                lobbyRepository.delete(otherLobby);
                lobbyRepository.flush();
            } else {
                List<UserGetDTO> userGetDTOS = new ArrayList<>();
                for(User u: otherLobby.getPlayers()) {
                    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(u);
                    userGetDTOS.add(userGetDTO);
                }
                lobbyRepository.saveAndFlush(otherLobby);
                messageService.sendToLobby(otherLobby.getLobbyId(), "userLeft", userGetDTOS);
            }
        }
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
        handlePlayerAlreadyInOtherLobby(user);
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
        List<UserGetDTO> userGetDTOS = new ArrayList<>();
        for(User u: lobby.getPlayers()) {
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(u);
            userGetDTOS.add(userGetDTO);
        }
        messageService.sendToLobby(lobby.getLobbyId(),"userLeft",userGetDTOS );
    }

    private void changePrincipal(User user, Player player, Lobby lobby) {
        if(user.getPrincipalName().equals(player.getUser().getPrincipalName())) {
            return;
        }
        player.getUser().setPrincipalName(user.getPrincipalName());
        lobbyRepository.saveAndFlush(lobby);
    }

    public void updateUser(User user) {
        Lobby lobby = lobbyRepository.findByPlayersContaining(user);
        if(lobby == null) {
            return;
        }
        for(User savedUser: lobby.getPlayers()) {
            if(user.getId().equals(savedUser.getId())) {
                updatePrincipalName(lobby,savedUser, user);
            }
        }
    }

    private void updatePrincipalName(Lobby lobby,User savedUser, User newUser) {
        if(!(savedUser.getPrincipalName().equals(newUser.getPrincipalName()))) {
            String oldPrincipal = savedUser.getPrincipalName();
            savedUser.setPrincipalName(newUser.getPrincipalName());
            lobbyRepository.saveAndFlush(lobby);
            log.info("updated user {}. Set principal name from {} to {}", savedUser.getUsername(), oldPrincipal, savedUser.getPrincipalName());
        }
    }



    public void setGame(Lobby lobby, Game game) {
        lobby.setGame(game);
        lobbyRepository.saveAndFlush(lobby);
    }

    public void destroyLobby(Game game) {
        Lobby lobby = lobbyRepository.findByGame(game);
        lobbyRepository.delete(lobby);
    }
}
