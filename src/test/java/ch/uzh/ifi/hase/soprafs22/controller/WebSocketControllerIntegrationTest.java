package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketControllerIntegrationTest {
    @LocalServerPort
    private Integer port;


    private WebSocketStompClient webSocketStompClient;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;
    @Qualifier("lobbyRepository")
    @Autowired
    private LobbyRepository lobbyRepository;
    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
        this.webSocketStompClient.setMessageConverter(new MappingJackson2MessageConverter());

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

    @Test void verify_OnConnection_principalIsConnectedToUser() throws InterruptedException {
        //used to await async calls
        BlockingQueue<String> blockingQueue = new LinkedBlockingDeque<>();

        String token = "token";

        User user = new User();
        user.setUsername("test");
        user.setPassword("test");
        user.setStatus(UserStatus.ONLINE);
        user.setToken(token);

        userRepository.save(user);
        userRepository.flush();



        StompSession session = connectWebsocket(token);
        blockingQueue.poll(1, SECONDS);

        User receivedUser = userRepository.findByToken(token);

        assertEquals(user.getUsername(), receivedUser.getUsername(), "wrong username");
        assertEquals(user.getPassword(), receivedUser.getPassword(), "wrong password");
        assertEquals(user.getToken(), receivedUser.getToken(), "wrong token");
        assertNotNull(receivedUser.getPrincipalName(), "Principal name not set");

    }

    @Test
    public void whenCallingCreateLobbyEndpoint_thenLobbyCreatedAndLobbyIdReturned() throws InterruptedException {

        BlockingQueue<LobbyPostDTO> blockingQueue = new LinkedBlockingDeque<>();
        //webSocketStompClient.setMessageConverter(new StringMessageConverter());

        String token = "token";

        User user = new User();
        user.setUsername("test");
        user.setPassword("test");
        user.setStatus(UserStatus.ONLINE);
        user.setToken(token);


        userRepository.save(user);
        userRepository.flush();

        StompSession session = connectWebsocket(token);
        //wait for connection
        blockingQueue.poll(1, SECONDS);

        session.subscribe("/users/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                System.out.println("accessed");
                return LobbyPostDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("Received message: " + payload);
                blockingQueue.add((LobbyPostDTO) payload);
            }
        });

        //wait for subscription
        System.out.println(blockingQueue.poll(1, SECONDS));
        session.send("/app/createLobby","");
        LobbyPostDTO dto = blockingQueue.poll(1, SECONDS);
        assertNotNull(dto.getLobbyId(), "lobbyId is null");
        Lobby createdLobby = lobbyRepository.findByLobbyId(dto.getLobbyId());
        Vector<User> players = createdLobby.getPlayers();
        assertEquals(user.getId(), players.get(0).getId());


    }
}