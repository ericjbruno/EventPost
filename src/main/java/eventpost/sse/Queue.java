package eventpost.sse;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import eventpost.persist.MessagePersistance;
import eventpost.persist.MessagePersistanceFactory;

/**
 * @author ericjbruno
 */
public class Queue extends Destination {

    // Unbounded, since every persisted message must have its ID queued for
    // delivery. A fixed capacity rejected IDs once full (stranding messages
    // that were already persisted) and failed to reload a large backlog.
    // A deque, so a message taken for a receiver that has gone can be put
    // back at the front
    final protected LinkedBlockingDeque<Long> messageIds
            = new LinkedBlockingDeque<>();

    final protected MessagePersistance persistance
            = MessagePersistanceFactory
                    .getInstance()
                    .getMessagePersistance();

    // Last message ID issued for this queue. IDs must be unique per queue
    // since they're the persistence key, so never reuse one even when
    // several messages arrive within the same millisecond
    final protected AtomicLong lastMessageId = new AtomicLong(0);

    public Queue(String name) {
        super(name);

        // load message IDs
        ArrayList<Long> ids = persistance.getMessageIds(name);
        if (ids != null) {
            messageIds.addAll(ids);
            for (Long id : ids) {
                lastMessageId.accumulateAndGet(id, Math::max);
            }
        }

    }

    public Message getNextMessage() throws InterruptedException {
        // Blocking call
        Long messageId = messageIds.takeFirst();

        // Load the message data using the ID
        Message message = persistance.getMessage(getName(), messageId);
        return message;
    }

    // Waits up to the timeout for the next message, returning null if none
    // arrives, so a caller can check between waits whether its receiver is
    // still connected
    public Message pollNextMessage(long timeout, TimeUnit unit) throws InterruptedException {
        Long messageId = messageIds.pollFirst(timeout, unit);
        if (messageId == null) {
            return null;
        }

        Message message = persistance.getMessage(getName(), messageId);
        if (message == null) {
            // The body is missing from storage; nothing to deliver
            return null;
        }
        return message;
    }

    // Puts back a message that was taken but not delivered, at the front of
    // the queue so it keeps its place
    public void returnMessage(Long messageId) {
        messageIds.addFirst(messageId);
    }

    public boolean addMessage(String message) {
        Long messageId = nextMessageId();

        try {
            // Persist the message body then save just the message id to the queue
            //
            if (persistance.saveMessage(getName(), messageId, message)) {
                if (messageIds.add(messageId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    protected long nextMessageId() {
        // Use the current time, or last ID + 1 if the clock hasn't advanced
        return lastMessageId.updateAndGet(
                last -> Math.max(last + 1, System.currentTimeMillis()));
    }

    public void deleteMessage(Long messageId) {
        persistance.deleteMessage(messageId, name);
    }
}
