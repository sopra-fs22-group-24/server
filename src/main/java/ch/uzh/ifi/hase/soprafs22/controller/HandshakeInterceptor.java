package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.service.LobbyService;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

@Component
public class HandshakeInterceptor extends HttpSessionHandshakeInterceptor {



    HandshakeInterceptor() {}
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String,Object> attributes)
            throws Exception {
        System.out.println(request.getHeaders());
        System.out.println(response);
        System.out.println(wsHandler);
        System.out.println(attributes);

        return true;
    }
}
