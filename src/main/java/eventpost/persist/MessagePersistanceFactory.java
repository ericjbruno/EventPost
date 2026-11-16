package eventpost.persist;

import eventpost.Settings;

/**
 * @author ericjbruno
 */
public class MessagePersistanceFactory {

    // Selects the message store: "noheapdb" (the default) or "oraclekv".
    // Set with -Deventpost.persistence=... or the EVENTPOST_PERSISTENCE
    // environment variable. The old messageserver.persistence and
    // MESSAGESERVER_PERSISTENCE names still work; see Settings
    public static final String PERSISTENCE_SETTING = "persistence";
    public static final String PERSISTENCE_PROPERTY = Settings.propertyName(PERSISTENCE_SETTING);
    public static final String PERSISTENCE_ENV = Settings.envName(PERSISTENCE_SETTING);

    private MessagePersistance db = null;

    private static MessagePersistanceFactory INSTANCE = new MessagePersistanceFactory();

    public static MessagePersistanceFactory getInstance() {
        return INSTANCE;
    }

    private MessagePersistanceFactory() {
        String type = Settings.get(PERSISTENCE_SETTING);
        if (type == null) {
            type = "noheapdb";
        }

        // Only the selected implementation's class is loaded, so the other
        // database never needs to be running
        switch (type.trim().toLowerCase()) {
            case "oraclekv":
                db = MessageNoSQL.getInstance();
                break;
            case "noheapdb":
                db = MessageNoHeapDB.getInstance();
                break;
            default:
                throw new IllegalArgumentException("Unknown " + PERSISTENCE_PROPERTY
                        + " '" + type + "'; expected 'oraclekv' or 'noheapdb'");
        }

        log("Using message persistence: " + type);
    }

    public MessagePersistance getMessagePersistance() {
        return db;
    }

    public static void log(String s) {
        System.out.println("[MessagePersistanceFactory] " + s);
    }
}
