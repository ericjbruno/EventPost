/*
 * Copyright 2019-2026 Eric J. Bruno, Allure Technology, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
