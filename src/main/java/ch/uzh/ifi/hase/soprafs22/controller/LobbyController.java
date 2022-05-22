package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.Player;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.GameException;
import ch.uzh.ifi.hase.soprafs22.rest.dto.CreateLobbyDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import ch.uzh.ifi.hase.soprafs22.service.MessageService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@Controller
public class LobbyController {
    private final Logger log = LoggerFactory.getLogger(LobbyController.class);
    private final LobbyService lobbyService;
    private final UserService userService;
    private final MessageService messageService;


    LobbyController(LobbyService lobbyService, UserService userService, MessageService messageService)
    {
        this.lobbyService = lobbyService;
        this.userService = userService;
        this.messageService = messageService;
    }


    @PostMapping("/lobby")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public LobbyPostDTO createLobby(@RequestHeader("Authorization") String token, CreateLobbyDTO createLobbyDTO) {
        User user = userService.authenticateUser(token);
        int maxSize = createLobbyDTO.getMaxSize();
        Lobby lobby = lobbyService.createLobby(user, maxSize);
        return DTOMapper.INSTANCE.convertEntityToLobbyPostDTO(lobby);
    }

    @GetMapping("/lobby")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<LobbyGetDTO> getAllLobbies(@RequestHeader("Authorization") String token) {
        userService.authenticateUser(token);
        List<Lobby> lobbies = lobbyService.getLobbies();
        List<LobbyGetDTO> lobbyGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (Lobby lobby : lobbies) {
            lobbyGetDTOs.add(DTOMapper.INSTANCE.convertEntityToLobbyGetDTO(lobby));
        }
        return lobbyGetDTOs;

    }

    @MessageMapping("/lobby/{lobbyId}/joinLobby")
    public void handle(StompHeaderAccessor accessor, @DestinationVariable("lobbyId") long lobbyId) {
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        log.info("/joinLobby. User {} wants to join lobby id {}", user.getUsername(),lobbyId);
        Lobby lobby;
        try {
            lobby = lobbyService.joinLobby(user, lobbyId);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e);
            return;
        }
        LobbyPostDTO returnDto = DTOMapper.INSTANCE.convertEntityToLobbyPostDTO(lobby);

        //Inform user
        messageService.sendToUser(user.getPrincipalName(),"joinLobby",returnDto);
        UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        //Inform lobby of users
        List<UserGetDTO> userGetDTOS = new ArrayList<>();
        for(User u: lobby.getPlayers()) {
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(u);
            userGetDTOS.add(userGetDTO);
        }
        messageService.sendToLobby(lobby.getLobbyId(), "userJoined",userGetDTOS);
        log.info("/joinLobby. User {} joined lobby id {}", user.getUsername(),lobby.getLobbyId());

    }

    @MessageMapping("/createLobby")
    public void createLobby(StompHeaderAccessor accessor,CreateLobbyDTO createLobbyDTO) {

        // authorize User
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        int maxSize = createLobbyDTO.getMaxSize();

        Lobby lobby = lobbyService.createLobby(user,maxSize);
        LobbyPostDTO dto = DTOMapper.INSTANCE.convertEntityToLobbyPostDTO(lobby);
        messageService.sendToUser(user.getPrincipalName(), "joinLobby", dto);
        log.info("created Lobby {} for {}",lobby.getLobbyId(),user.getUsername());
    }

    @MessageMapping("/lobby/{lobbyId}/leaveLobby")
    public void leaveLobby(StompHeaderAccessor accessor, @DestinationVariable("lobbyId") long lobbyId) {
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        try {
            lobbyService.leaveLobby(lobbyId, user);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e);
        }

    }

}
