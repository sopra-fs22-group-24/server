package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.Lobby;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.entity.deck.Symbol;
import ch.uzh.ifi.hase.soprafs22.repository.LobbyRepository;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs22.rest.dto.UserGetDTO;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LobbyControllerIntegrationTest {
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
        lobbyRepository.deleteAll();
        userRepository.deleteAll();
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

        String token = "token1";

        User user = new User();
        user.setUsername("test1");
        user.setPassword("test1");
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

        String token = "token2";

        User user = new User();
        user.setUsername("test2");
        user.setPassword("test2");
        user.setStatus(UserStatus.ONLINE);
        user.setToken(token);


        userRepository.save(user);
        userRepository.flush();

        StompSession session = connectWebsocket(token);
        //wait for connection
        blockingQueue.poll(1, SECONDS);

        session.subscribe("/users/queue/joinLobby", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LobbyPostDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((LobbyPostDTO) payload);
            }
        });

        //wait for subscription
        session.send("/app/createLobby","");
        LobbyPostDTO dto = blockingQueue.poll(1, SECONDS);
        assertNotNull(dto.getLobbyId(), "lobbyId is null");
        Lobby createdLobby = lobbyRepository.findByLobbyId(dto.getLobbyId());
        Vector<User> players = createdLobby.getPlayers();
        assertEquals(user.getId(), players.get(0).getId());


    }

    @Test
    public void whenCallingJoinLobbyEndpointWithValidLobbyId_thenUserIsAddedToLobbyAndReceivesMessage() throws InterruptedException {
        //Setup
        BlockingQueue<LobbyPostDTO> blockingQueue = new LinkedBlockingDeque<>();
        BlockingQueue<LobbyPostDTO> blockingQueue2 = new LinkedBlockingDeque<>();
        BlockingQueue<List<UserGetDTO>> blockingQueue3 = new LinkedBlockingDeque<>();
        BlockingQueue<List<UserGetDTO>> blockingQueue4 = new LinkedBlockingDeque<>();


        String token1 = "token3";

        User user1 = new User();
        user1.setUsername("test3");
        user1.setPassword("test3");
        user1.setStatus(UserStatus.ONLINE);
        user1.setToken(token1);
        userRepository.save(user1);

        String token2 = "token4";

        User user2 = new User();
        user2.setUsername("test4");
        user2.setPassword("test4");
        user2.setStatus(UserStatus.ONLINE);
        user2.setToken(token2);
        userRepository.save(user2);
        userRepository.flush();

        StompSession session = connectWebsocket(token1);
        //wait for connection
        blockingQueue.poll(1, SECONDS);

        session.subscribe("/users/queue/joinLobby", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LobbyPostDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.add((LobbyPostDTO) payload);
            }
        });


        //wait for subscription
        session.send("/app/createLobby","");
        LobbyPostDTO dto = blockingQueue.poll(1, SECONDS);
        session.subscribe("/lobby/" + dto.getLobbyId() + "/userJoined", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                System.out.println("oi");
                return List.class;
                //return Object.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("oi2");
                ArrayList<UserGetDTO> userGetDTOS = new ArrayList<>();
                for(LinkedHashMap o: (List<LinkedHashMap>) payload) {
                    System.out.println("oi");
                    System.out.println(o);
                    UserGetDTO userGetDTO = new UserGetDTO();
                    Long.valueOf((int) o.get("id"));
                    userGetDTO.setId(Long.valueOf((int) o.get("id")));
                    userGetDTO.setUsername((String) o.get("username"));
                    userGetDTOS.add(userGetDTO);
                }
                blockingQueue3.add(userGetDTOS);
                return ;

            }
        });

        //test
        StompSession session2 = connectWebsocket(token2);
        //wait for connection
        blockingQueue.poll(1, SECONDS);

        session2.subscribe("/users/queue/joinLobby", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return LobbyPostDTO.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue2.add((LobbyPostDTO) payload);
            }
        });

        session2.subscribe("/lobby/" + dto.getLobbyId() + "/userJoined", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return List.class;
                //return Object.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("hello");
                System.out.println(payload);
                ArrayList<UserGetDTO> userGetDTOS = new ArrayList<>();
                for(LinkedHashMap o: (List<LinkedHashMap>) payload) {
                    System.out.println("oi");
                    System.out.println(o);
                    UserGetDTO userGetDTO = new UserGetDTO();
                    Long.valueOf((int) o.get("id"));
                    userGetDTO.setId(Long.valueOf((int) o.get("id")));
                    userGetDTO.setUsername((String) o.get("username"));
                    userGetDTOS.add(userGetDTO);
                }
                blockingQueue4.add(userGetDTOS);
            }
        });
        blockingQueue.poll(1, SECONDS);

        session2.send("/app/lobby/"+dto.getLobbyId()+"/joinLobby", dto);
        LobbyPostDTO dto2 = blockingQueue2.poll(1, SECONDS);
        List<UserGetDTO> userGetDTOS = blockingQueue3.poll(1,SECONDS);
        List<UserGetDTO> userGetDTOS2 = blockingQueue4.poll(1, SECONDS);
        Lobby receivedLobby = lobbyRepository.findByLobbyId(dto.getLobbyId());
        assertNotNull(receivedLobby.getLobbyId(), "lobbyId is null");
        Vector<User> players = receivedLobby.getPlayers();
        assertEquals(dto.getLobbyId(), dto2.getLobbyId(), "different lobby ids received");
        assertEquals(players.get(0).getId(), user1.getId(), "user1 is not in lobby");
        assertEquals(players.get(1).getId(),user2.getId(), "user2 is not in lobby");
        assertEquals(players.get(1).getId(), userGetDTOS.get(1).getId(), "dto id not the same as player dto");
        assertEquals(userGetDTOS.get(1).getUsername(), userGetDTOS2.get(1).getUsername(), "players got different messages");


    }

}