package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Card;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Color;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Symbol;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.*;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapperImpl;
import ch.uzh.ifi.hase.soprafs22.service.GameService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapping;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GameControllerTest {
    @LocalServerPort
    private Integer port;


    private WebSocketStompClient webSocketStompClient;

    @Mock
    GameService gameService;


    @BeforeEach
    public void setup() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

    }
    public User createUser(int n) {
        User u = new User();
        u.setUsername(String.format("test%d",n));
        u.setPassword(String.format("test%d",n));
        u.setToken(String.format("token%d",n));
        u.setStatus(UserStatus.ONLINE);
        System.out.println(n);
        return u;
    }
    private StompSession connectWebsocket(String token) {
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("token", token);

        StompSession session = null;
        try {
            session = webSocketStompClient
                    .connect(String.format("ws://localhost:%d/ws-connect", port),handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter() {
                        @Override
                        public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                            throw new RuntimeException("Failure in WebSocket handling", exception);
                        }
                    })
                    .get(1, SECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        catch (TimeoutException e) {
            e.printStackTrace();
        }
        return session;
    }
    @Test
    public void startGame_whenSuccessful_thenGameCreatedCorrectlyAndUserInformed() throws InterruptedException {

        User u1 = createUser(1);
        Lobby lobby = new Lobby();
        lobby.addUser(u1);


        BlockingQueue<GameIdDTO> bq1 = new LinkedBlockingDeque<>();

        StompSession session1 = connectWebsocket(u1.getToken());

        bq1.poll(1, SECONDS);

        Card card = new Card();
        card.setColor(Color.BLUE);
        card.setSymbol(Symbol.DISCARD_ALL);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername(u1.getUsername());

        UNODTO unodto = new UNODTO();
        unodto.setUno(true);
        PlayCardDTO playCardDTO = new PlayCardDTO();
        playCardDTO.setCard(card);
        playCardDTO.setUser(u1);
        playCardDTO.setUno(true);
        session1.send("/app/game/1/playCard", playCardDTO);
    }
}
