package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.GameException;
import ch.uzh.ifi.hase.soprafs22.rest.dto.*;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.MessageService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class GameController {
    private final Logger log = LoggerFactory.getLogger(GameController.class);

    private final UserService userService;
    private final GameService gameService;
    private final MessageService messageService;


    GameController(UserService userService, GameService gameService, MessageService messageService)
    {
        this.userService = userService;
        this.gameService = gameService;
        this.messageService = messageService;
    }


    /*
    Receives lobbyId, creates game, returns gameId
     */
    @MessageMapping("/game")
    public void startGame(StompHeaderAccessor accessor, LobbyPostDTO dto) {
        if(accessor.getUser()==null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());

        log.info("User {} in lobby {} wants to start game", user.getUsername(), dto.getLobbyId());
        long lobbyId = dto.getLobbyId();
        try {
            Game game = gameService.createGame(lobbyId, user);
            GameIdDTO gameDto = DTOMapper.INSTANCE.convertGameToGameIdDTO(game);
            messageService.sendToLobby(lobbyId, "startGame",gameDto);
            log.info("Successfully created game {} for lobby {} by user {}", game.getGameId(), lobbyId, user.getUsername());
        } catch (GameException e) {
            log.info("Error in /game: {} ",e.getClass().getSimpleName());
            messageService.sendErrorToUser(user.getPrincipalName(), e);
        }



    }
    // TODO EndPoint Complain if it works player who called uno draws automatically
    @MessageMapping("/game/{gameId}/callOut")
    public void callOut(StompHeaderAccessor accessor, UserPostDTO dto, @DestinationVariable("gameId") long gameId) {
        if(accessor.getUser()==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        User calledOutUser = userService.getUserByUsername(dto.getUsername());
        try {
            gameService.callOutPlayer(gameId, user, calledOutUser );
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e);
        }
    }

    // TODO additionaly take optional UNO called argument ( then it will be possible to complain until next move is over)
    // TODO optional PlayerArgument In playCard for extremeHitcard card

    /*
    Will only be used if player forgot to say uno while playing a card.
     */
    @MessageMapping("/game/{gameId}/uno")
    public void sayUno(StompHeaderAccessor accessor, @DestinationVariable("gameId") long gameId) {
        if(accessor.getUser()==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());

        gameService.sayUnoAfterPlaying(gameId, user);
    }

    @MessageMapping("/game/{gameId}/playCard")
    public void playCard(StompHeaderAccessor accessor, PlayCardDTO playCardDTO, @DestinationVariable("gameId") long gameId) {
        if(accessor.getUser()==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        Card card  = playCardDTO.getCard();
        User otherUser;
        try {
            otherUser = userService.getUserByUsername(playCardDTO.getUser().getUsername());
        } catch (NullPointerException e) {
            otherUser = null;
        }
        boolean uno = playCardDTO.getUno();
        try {
            gameService.playCard(gameId, user, card, otherUser, uno);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e);
        }
    }

    @MessageMapping("/game/{gameId}/init")
    public void init(StompHeaderAccessor accessor, @DestinationVariable("gameId") long gameId) {
        if(accessor.getUser()==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        try {
            gameService.initialize(gameId, user);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e);
        }
    }

    @MessageMapping("/game/{gameId}/drawCard")
    public void drawCard(StompHeaderAccessor accessor, @DestinationVariable("gameId") long gameId) {
        if(accessor.getUser()==null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        log.info("user {} wants to draw", user.getUsername());
        try {
            gameService.drawCard(gameId, user);
        } catch (GameException e) {
            messageService.sendErrorToUser(user.getPrincipalName(), e);
        }
    }

    //Disables all card checks in game
    @MessageMapping("game/{gameId}/enableSudo")
    public void enableSudo(StompHeaderAccessor accessor, @DestinationVariable("gameId") long gameId) {
        gameService.enableSudo(gameId);
        log.info("sudo enabled in {}", gameId);
    }
}
