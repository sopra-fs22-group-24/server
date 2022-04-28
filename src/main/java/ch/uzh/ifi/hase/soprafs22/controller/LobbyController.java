package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.GameException;
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
    public LobbyPostDTO createLobby(@RequestHeader("Authorization") String token) {
        User user = userService.authenticateUser(token);
        Lobby lobby = lobbyService.createLobby(user);
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
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
            return;
        }
        LobbyPostDTO returnDto = DTOMapper.INSTANCE.convertEntityToLobbyPostDTO(lobby);

        //Inform user
        messageService.sendToUser(user.getPrincipalName(),"joinLobby",returnDto);
        UserGetDTO userDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        //Inform lobby

        messageService.sendToLobby(lobby.getLobbyId(), "userJoined",userDTO);
        log.info("/joinLobby. User {} joined lobby id {}", user.getUsername(),lobby.getLobbyId());

    }

    @MessageMapping("/createLobby")
    public void createLobby(StompHeaderAccessor accessor) {

        // authorize User
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());


        Lobby lobby = lobbyService.createLobby(user);
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
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
        }

    }

}
