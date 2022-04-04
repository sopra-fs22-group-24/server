package ch.uzh.ifi.hase.soprafs22.controller;

import ch.uzh.ifi.hase.soprafs22.repository.UserRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class HandshakeInterceptor  {

    private UserRepository userRepository;

    public HandshakeInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener
    private void handleSessionConnect(SessionConnectedEvent event) {
        System.out.println("halleluja");
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());

    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {
        System.out.println("byebye");
        /*
        Optional.ofNullable(userRepository.getParticipant(event.getSessionId()))
                .ifPresent(login -> {
                    messagingTemplate.convertAndSend(logoutDestination, new LogoutEvent(login.getUsername()));
                    participantRepository.removeParticipant(event.getSessionId());
                });

         */
    }

}
