package ch.uzh.ifi.hase.soprafs22.utils;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;

public class StompHeaderUtil {

    public static String getNativeHeaderField(StompHeaderAccessor accessor, String fieldName) {
        GenericMessage<?> generic = (GenericMessage<?>) accessor.getHeader(SimpMessageHeaderAccessor.CONNECT_MESSAGE_HEADER);
        if (generic != null) {
            SimpMessageHeaderAccessor nativeAccessor = SimpMessageHeaderAccessor.wrap(generic);
            List<String> value = nativeAccessor.getNativeHeader(fieldName);

            return value == null ? null : value.stream().findFirst().orElse(null);
        }

        return null;
    }


}
