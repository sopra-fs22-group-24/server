package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketControllerTest {
    @LocalServerPort
    private Integer port;


    private WebSocketStompClient webSocketStompClient;

    @Qualifier("userRepository")
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    private StompSession connectWebsocket(String token) {
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("token", token);

        StompSession session = null;
        try {
            session = webSocketStompClient
                    .connect(String.format("ws://localhost:%d/ws-connect", port),handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter() {
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
        BlockingQueue<String> blockingQueue = new ArrayBlockingQueue(1);

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
}