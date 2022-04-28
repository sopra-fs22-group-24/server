
package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.entity.User;
import ch.uzh.ifi.hase.soprafs22.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;


@Component
public class HandshakeHandler extends DefaultHandshakeHandler {
    // Custom class for storing principal
    @Autowired
    UserService userService;

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        // Generate principal with UUID as name
        StompPrincipal principal = new StompPrincipal(UUID.randomUUID().toString());
        System.out.println(principal.getName());
        return principal;
    }

    @EventListener
    private void handleSessionConnectedListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String token = getUserId(accessor, "token");

        Principal principal = accessor.getUser();
        User user = userService.getUserByToken(token);
        if(user==null) {
            return;
        }
        userService.addPrincipalName(user, principal.getName());


    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {

    }

    private String getUserId(StompHeaderAccessor accessor, String fieldName) {
        LinkedMultiValueMap generic = (LinkedMultiValueMap) accessor.getHeader(SimpMessageHeaderAccessor.NATIVE_HEADERS);
        return (String) generic.get(fieldName).get(0);

    }
}