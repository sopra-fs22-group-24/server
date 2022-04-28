package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.GameException;
import ch.uzh.ifi.hase.soprafs22.rest.dto.*;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import ch.uzh.ifi.hase.soprafs22.service.MessageService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;


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
        log.info("User {} in lobby {} wants to start game", user.getUsername(), dto.getLobbyId());
        long lobbyId = dto.getLobbyId();
        try {
            Game game = gameService.createGame(lobbyId, user);
            GameIdDTO gameDto = DTOMapper.INSTANCE.convertGameToGameIdDTO(game);
            messageService.sendToLobby(lobbyId, "startGame",gameDto);
            log.info("Successfully created game {} for lobby {} by user {}", game.getGameId(), lobbyId, user.getUsername());
            //simpMessage.convertAndSend(String.format("/lobby/%d/messages", lobbyId), gameDto);
        } catch (GameException e) {
            log.info("Error in /game: {} ",e.getClass().getSimpleName());
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
        }



    }
    // TODO EndPoint Complain if it works player who called uno draws automatically


    // TODO additionaly take optional UNO called argument ( then it will be possible to complain until next move is over)
    // TODO optional PlayerArgument In playCard for extremeHitcard card
    @MessageMapping("/game/{gameId}/playCard")
//    public void playCard(StompHeaderAccessor accessor, CardDTO cardDTO,UserPostDTO otherUserDTO, UNODTO unoDTO, @DestinationVariable("gameId") long gameId) {
    public void playCard(StompHeaderAccessor accessor, PlayCardDTO playCardDTO, @DestinationVariable("gameId") long gameId) {
        System.out.println("hello");
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        Card card  = playCardDTO.getCard();
        User otherUser = playCardDTO.getUser();
        boolean uno = playCardDTO.getUno();
        try {
            gameService.playCard(gameId, user, card, otherUser, uno);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
        }
    }

    @MessageMapping("/game/{gameId}/init")
    public void init(StompHeaderAccessor accessor, @DestinationVariable("gameId") long gameId) {
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        try {
            gameService.initialize(gameId, user);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
        }
    }

    @MessageMapping("/game/{gameId}/drawCard")
    public void drawCard(StompHeaderAccessor accessor, @DestinationVariable("gameId") long gameId) {
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        try {
            gameService.drawCard(gameId, user);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e.getClass().getSimpleName());
        }
    }
}
