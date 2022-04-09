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
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import ch.uzh.ifi.hase.soprafs22.utils.StompHeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import java.security.Principal;

@Controller
public class WebSocketController {
    private final GameService gameService;
    private final UserService userService;
    private final LobbyService lobbyService;
    private final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate simpMessage;
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
        log.info("/joinLobby with id {}", sessionId);

        System.out.println(sessionId);
        System.out.println(accessor.getUser().getName());
        Principal p = accessor.getUser();
        Message m1 = new Message("principal");
        Message m2 = new Message("sessionId");

        simpMessage.convertAndSendToUser(p.getName(), "/queue/messages", m1);
        simpMessage.convertAndSendToUser(sessionId, "/queue/messages", m2);

        //log.info("Replied to {} with message {}",sessionId, m);
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
        log.info("created Lobby {} for {}",lobby.getLobbyId(),user.getUsername());
        simpMessage.convertAndSendToUser(accessor.getUser().getName(), "/queue/messages", dto );
    }

}
