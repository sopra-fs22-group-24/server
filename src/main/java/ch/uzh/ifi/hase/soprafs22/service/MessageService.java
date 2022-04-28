package ch.uzh.ifi.hase.soprafs22.service;


import ch.uzh.ifi.hase.soprafs22.rest.dto.ErrorDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public void sendToGame(long gameId, String destination, Object payload) {
        log.info("Sending to {}, payload: {}","/game/"+gameId+"/"+destination,payload);
        simpMessage.convertAndSend("/game/"+gameId+"/"+destination, payload);

    }

    public void sendToUser(String principal, String destination,Object payload) {
        simpMessage.convertAndSendToUser(principal, "/queue/"+destination, payload);
    }

    public void sendToLobby(long lobbyId, String destination,Object payload) {
        log.info("Sending to {} message {}","/lobby/"+lobbyId+"/"+destination,payload);
        simpMessage.convertAndSend("/lobby/"+lobbyId+"/"+destination, payload);
    }

    public void sendErrorToUser(String principal, String error) {
        ErrorDTO err = new ErrorDTO();
        err.setError(error);
        simpMessage.convertAndSendToUser(principal, "/queue/error", err);
    }


}
