package eventpost.persist;

import java.util.ArrayList;
import eventpost.sse.Message;

/**
 * @author ericjbruno
 */
public interface MessagePersistance {

    public boolean saveMessage(String destinationName,
            Long messageId,
            String message) throws Exception;

    public Message getMessage(String destinationName, Long messageId);

    public String getNextMessage(String destinationName);

    public boolean deleteMessage(Long messageId, String destinationName);

    public ArrayList<String> getStoredDestinations();

    public ArrayList<Long> getMessageIds(String destinationName);
}
