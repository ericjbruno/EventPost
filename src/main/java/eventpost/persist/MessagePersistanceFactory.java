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
