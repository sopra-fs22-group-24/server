package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.Game;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.exceptions.gameExceptions.UserNotLobbyAdminException;
import ch.uzh.ifi.hase.soprafs22.messagingObjects.Message;
import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.ErrorDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.GameIdDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs22.rest.mapper.DTOMapperImpl;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.junit.jupiter.api.AfterEach;
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
class GameControllerIntegrationTest {
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

    @Qualifier("gameRepository")
    @Autowired
    private GameRepository gameRepository;


    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
        lobbyRepository.deleteAll();
        gameRepository.deleteAll();

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
        u = userRepository.save(u);
        userRepository.flush();
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
    public void startGame_whenSuccessfull_thenGameCreatedCorrectlyAndUserInformed() throws InterruptedException {
        User u1 = createUser(1);
        User u2 = createUser(2);

        Lobby lobby = new Lobby();
        lobby.addUser(u1);
        lobby.addUser(u2);

        lobby = lobbyRepository.save(lobby);

        BlockingQueue<GameIdDTO> bq1 = new LinkedBlockingDeque<>();
        BlockingQueue<GameIdDTO> bq2 = new LinkedBlockingDeque<>();

        StompSession session1 = connectWebsocket(u1.getToken());
        StompSession session2 = connectWebsocket(u2.getToken());

        bq1.poll(1,SECONDS);
        session1.subscribe("/lobby/"+lobby.getLobbyId()+"/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameIdDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                bq1.add((GameIdDTO) payload);
            }
        });
        session2.subscribe("/lobby/"+lobby.getLobbyId()+"/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return GameIdDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("hello");
                bq2.add((GameIdDTO) payload);
            }
        });

        bq1.poll(1,SECONDS);
        LobbyPostDTO dto = DTOMapperImpl.INSTANCE.convertEntityToLobbyPostDTO(lobby);
        session1.send("/app/game",dto);
        GameIdDTO dto1 = bq1.poll(1,SECONDS);
        GameIdDTO dto2 = bq2.poll(1, SECONDS);

        Game game = gameRepository.findByGameId(dto1.getGameId());

        assertEquals(dto1.getGameId(), dto2.getGameId());
        assertEquals(game.getPlayers().get(0).getUser().getId(),u1.getId());
        assertEquals(game.getPlayers().get(1).getUser().getId(),u2.getId());
        assertEquals(game.getPlayers().get(0).getHand().getCardCount(),7);
        assertEquals(game.getPlayers().get(1).getHand().getCardCount(),7);
    }

    @Test
    public void startGame_whenUserNotAdmin_thenSendError() throws InterruptedException {
        User u1 = createUser(1);
        User u2 = createUser(2);

        Lobby lobby = new Lobby();
        lobby.addUser(u1);
        lobby.addUser(u2);

        lobby = lobbyRepository.save(lobby);

        BlockingQueue<ErrorDTO> bq2 = new LinkedBlockingDeque<>();

        StompSession session2 = connectWebsocket(u2.getToken());

        bq2.poll(1,SECONDS);

        session2.subscribe("/users/queue/messages", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ErrorDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("hello");
                bq2.add((ErrorDTO) payload);
            }
        });

        bq2.poll(1,SECONDS);
        LobbyPostDTO dto = DTOMapperImpl.INSTANCE.convertEntityToLobbyPostDTO(lobby);
        session2.send("/app/game",dto);
        ErrorDTO dto2 = bq2.poll(1, SECONDS);

        assertEquals(dto2.getError(), UserNotLobbyAdminException.class.getSimpleName());

    }
}