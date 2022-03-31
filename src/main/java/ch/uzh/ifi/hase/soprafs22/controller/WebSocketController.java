package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Color;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Symbol;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Message;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Response;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Hello;
import ch.uzh.ifi.hase.soprafs22.rest.dto.CardDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {
    private final GameService gameService;

    WebSocketController(GameService gameService) {
        this.gameService = gameService;
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




}
