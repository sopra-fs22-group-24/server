package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Color;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Symbol;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Message;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Response;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Hello;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Controller
public class LobbyController {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final LobbyService lobbyService;
    private final UserService userService;
    @Autowired
    private SimpMessagingTemplate simpMessage;

    LobbyController(LobbyService lobbyService, UserService userService)
    {
        this.lobbyService = lobbyService;
        this.userService = userService;
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

    @MessageMapping("/joinLobby")
    public void handle(StompHeaderAccessor accessor, LobbyPostDTO dto) {
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        log.info("/joinLobby. User {} wants to join lobby id {}", user.getUsername(),dto.getLobbyId());

        Lobby lobby = lobbyService.joinLobby(user, dto.getLobbyId());
        Message m = new Message(String.format("joined lobby %d",lobby.getLobbyId()));
        simpMessage.convertAndSendToUser(user.getPrincipalName(), "/queue/messages", m);
        log.info("/joinLobby. User {} joined lobby id {}", user.getUsername(),lobby.getLobbyId());

    }

    @MessageMapping("/createLobby")
    public void createLobby(StompHeaderAccessor accessor) {

        // authorize User
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        if(user == null) {
            Message m = new Message("Error: User doesn't exist");
            simpMessage.convertAndSendToUser(accessor.getUser().getName(), "/queue/messages", m);

        }
        Lobby lobby = lobbyService.createLobby(user);
        LobbyPostDTO dto = DTOMapper.INSTANCE.convertEntityToLobbyPostDTO(lobby);
        simpMessage.convertAndSendToUser(accessor.getUser().getName(), "/queue/messages", dto );
        log.info("created Lobby {} for {}",lobby.getLobbyId(),user.getUsername());
    }



}
