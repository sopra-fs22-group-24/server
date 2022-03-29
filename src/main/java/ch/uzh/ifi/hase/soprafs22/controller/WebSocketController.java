package ch.uzh.ifi.hase.soprafs22.controller;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Response;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Hello;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebSocketController {


    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Response greeting(Hello message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Response("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }




}
