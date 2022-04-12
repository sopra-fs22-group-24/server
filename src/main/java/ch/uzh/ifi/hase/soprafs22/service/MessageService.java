package ch.uzh.ifi.hase.soprafs22.service;

import ch.uzh.ifi.hase.soprafs22.repository.GameRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MessageService {
    @Autowired
    private SimpMessagingTemplate simpMessage;

    private final Logger log = LoggerFactory.getLogger(UserService.class);



    @Autowired
    public MessageService() {};

    public void sendToGame(long gameId, Object payload) {
        simpMessage.convertAndSend("/game/"+gameId+"/messages", payload);

    }

    public void sendToUser(String principal, Object payload) {
        simpMessage.convertAndSendToUser(principal, "/queue/messages", payload);
    }

    public void sendToLobby(long lobbyId, Object payload) {
        simpMessage.convertAndSend("/lobby/"+lobbyId+"/messages", payload);
    }


}
