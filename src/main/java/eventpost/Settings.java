package eventpost;

/**
 * Reads EventPost's settings. Each setting can be given as a system property
 * (-Deventpost.x.y=...) or an environment variable (EVENTPOST_X_Y). The names
 * used before the rename to EventPost (messageserver.x.y and MESSAGESERVER_X_Y)
 * still work, so existing scripts don't need changing.
 *
 * @author ericjbruno
 */
public final class Settings {

    private static final String PREFIX = "eventpost.";
    private static final String OLD_PREFIX = "messageserver.";

    private Settings() {
    }

    /**
     * Returns the setting's value, or null when it isn't set.
     *
     * @param name the property name without the prefix, such as "persistence"
     */
    public static String get(String name) {
        String value = lookup(PREFIX + name);
        if (value == null) {
            value = lookup(OLD_PREFIX + name);
        }
        return value;
    }

    /** The system property name for a setting, such as "eventpost.persistence". */
    public static String propertyName(String name) {
        return PREFIX + name;
    }

    /** The environment variable name for a setting, such as "EVENTPOST_PERSISTENCE". */
    public static String envName(String name) {
        return toEnv(PREFIX + name);
    }

    private static String lookup(String property) {
        String value = System.getProperty(property);
        if (value == null || value.isBlank()) {
            value = System.getenv(toEnv(property));
        }
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private static String toEnv(String property) {
        return property.replace('.', '_').toUpperCase();
    }
}
