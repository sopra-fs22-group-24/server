package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Color;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Symbol;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.GameException;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Message;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Response;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Hello;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.*;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import ch.uzh.ifi.hase.soprafs22.service.MessageService;
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
public class GameController {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final LobbyService lobbyService;
    private final UserService userService;
    private final GameService gameService;
    private final MessageService messageService;
    @Autowired
    private SimpMessagingTemplate simpMessage;

    GameController(LobbyService lobbyService, UserService userService, GameService gameService, MessageService messageService)
    {
        this.lobbyService = lobbyService;
        this.userService = userService;
        this.gameService = gameService;
        this.messageService = messageService;
    }


    /*
    Receives lobbyId, creates game, returns gameId
     */
    @MessageMapping("/game")
    public void startGame(StompHeaderAccessor accessor, LobbyPostDTO dto) {
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());

        long lobbyId = dto.getLobbyId();

        try {
            Game game = gameService.createGame(lobbyId, user);
            GameIdDTO gameDto = DTOMapper.INSTANCE.convertGameToGameIdDTO(game);
            messageService.sendToLobby(lobbyId,gameDto);
            simpMessage.convertAndSend(String.format("/lobby/%d/messages", lobbyId), gameDto);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
        }


    }

    @MessageMapping("/game/{gameId}/playCard")
    public void playCard(StompHeaderAccessor accessor, Card card,@PathVariable("gameId") long gameId) {
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        try {
            gameService.playCard(gameId, user, card);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
        }
    }



}
