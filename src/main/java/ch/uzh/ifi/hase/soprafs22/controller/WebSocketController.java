package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Color;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Symbol;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Message;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Response;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Hello;
import ch.uzh.ifi.hase.soprafs22.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import ch.uzh.ifi.hase.soprafs22.utils.StompHeaderUtil;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@Controller
public class WebSocketController {
    private final GameService gameService;
    private final UserService userService;
    private final LobbyService lobbyService;
    WebSocketController(GameService gameService, UserService userService, LobbyService lobbyService) {

        this.gameService = gameService;
        this.userService = userService;
        this.lobbyService = lobbyService;
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Response greeting(Hello message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Response("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

    @MessageMapping("/startGame")
    @SendTo("/topic/messages")
    public CardDTO createGame() throws Exception {
        gameService.createGame();
        System.out.println("le start");

        CardDTO cardDTO = DTOMapper.INSTANCE.convertCardToCardDTO(new Card(Color.BLUE, Symbol.DISCARD_ALL));
        return cardDTO;
    }

    @MessageMapping("/joinLobby")
    public void handle(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        System.out.println(sessionId);
        System.out.println(accessor.getUser().getName());
        // authorize User
        User user = userService.getUserByPrincipalName(accessor.getUser().getName());
        String lobbyId = StompHeaderUtil.getNativeHeaderField(accessor, "lobbyId");
        Lobby lobby;
        if(lobbyId == null) {
            lobby = lobbyService.createLobby(user);
        } else {
            lobby = lobbyService.joinLobby(user, Long.parseLong(lobbyId));
        }
        System.out.println("hello");
    }


}
