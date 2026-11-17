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

import com.alluretechnology.noheapdb.DataStore;
import com.alluretechnology.noheapdb.NoHeapDB;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import eventpost.Settings;
import eventpost.sse.Message;

/**
 * Message persistence using NoHeapDB. Message bodies are kept off-heap in a
 * single NoHeapDB store, keyed by "destination/messageId"; only the message
 * IDs are kept on-heap.
 *
 * The store is IN_MEMORY by default, so messages don't survive a restart.
 * Set -Deventpost.noheapdb.storage=persisted (or the
 * EVENTPOST_NOHEAPDB_STORAGE environment variable) to keep them in files
 * under ~/JavaOffHeap, where they're reloaded when the server restarts.
 *
 * @author ericjbruno
 */
public class MessageNoHeapDB implements MessagePersistance {

    // The old messageserver.noheapdb.storage and MESSAGESERVER_NOHEAPDB_STORAGE
    // names still work; see Settings
    public static final String STORAGE_SETTING = "noheapdb.storage";
    public static final String STORAGE_PROPERTY = Settings.propertyName(STORAGE_SETTING);
    public static final String STORAGE_ENV = Settings.envName(STORAGE_SETTING);

    private static final String STORE_NAME = "messages";

    // Initial size; the journal adds chunks on its own as it fills up
    private static final long STORE_SIZE_MB = 256;

    private final NoHeapDB db = new NoHeapDB();

    // Each destination's message IDs, sorted, so a destination's messages can
    // be listed without scanning every key in the store
    private final ConcurrentHashMap<String, ConcurrentSkipListSet<Long>> destinations
            = new ConcurrentHashMap<>();

    private static MessageNoHeapDB INSTANCE = new MessageNoHeapDB();

    public static MessageNoHeapDB getInstance() {
        return INSTANCE;
    }

    private MessageNoHeapDB() {
        DataStore.Storage storage = getStorageType();
        try {
            // Reopens the existing files if the store was persisted before
            db.createStore(STORE_NAME, storage, STORE_SIZE_MB);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create NoHeapDB store", e);
        }

        if (storage == DataStore.Storage.PERSISTED) {
            loadMessageIds();

            // Close cleanly on shutdown so the next start can reuse the store's
            // index instead of rebuilding it from the journal
            Runtime.getRuntime().addShutdownHook(new Thread(() -> db.closeStore(STORE_NAME)));
        }

        log("Using " + storage + " store");
    }

    private static DataStore.Storage getStorageType() {
        String type = Settings.get(STORAGE_SETTING);
        if (type == null) {
            return DataStore.Storage.IN_MEMORY;
        }

        switch (type.trim().toLowerCase()) {
            case "in_memory":
                return DataStore.Storage.IN_MEMORY;
            case "persisted":
                return DataStore.Storage.PERSISTED;
            default:
                throw new IllegalArgumentException("Unknown " + STORAGE_PROPERTY
                        + " '" + type + "'; expected 'in_memory' or 'persisted'");
        }
    }

    private void loadMessageIds() {
        int count = 0;
        Iterator<String> keys = db.keys(STORE_NAME);
        while (keys.hasNext()) {
            String key = keys.next();
            int slash = key.lastIndexOf('/');
            try {
                Long messageId = Long.valueOf(key.substring(slash + 1));
                getIds(key.substring(0, slash)).add(messageId);
                count++;
            } catch (RuntimeException e) {
                log("Skipping unrecognized key '" + key + "'");
            }
        }
        log("Loaded " + count + " stored messages");
    }

    public boolean saveMessage(String destinationName,
            Long messageId,
            String message) throws Exception {
        if (!db.putString(STORE_NAME, toKey(destinationName, messageId), message)) {
            return false;
        }

        getIds(destinationName).add(messageId);
        return true;
    }

    public Message getMessage(String destinationName, Long messageId) {
        String data = db.getString(STORE_NAME, toKey(destinationName, messageId));
        if (data == null) {
            log("Message " + toKey(destinationName, messageId) + " is null!");
            return null;
        }

        return new Message(messageId, data);
    }

    public String getNextMessage(String destinationName) {
        return null;
    }

    public boolean deleteMessage(Long messageId, String destinationName) {
        Set<Long> ids = destinations.get(destinationName);
        if (ids == null || !ids.remove(messageId)) {
            return false;
        }

        return db.remove(STORE_NAME, toKey(destinationName, messageId));
    }

    @Override
    public ArrayList<String> getStoredDestinations() {
        ArrayList<String> names = new ArrayList<>();
        for (Map.Entry<String, ConcurrentSkipListSet<Long>> entry : destinations.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                names.add(entry.getKey());
            }
        }
        return names;
    }

    @Override
    public ArrayList<Long> getMessageIds(String destinationName) {
        Set<Long> ids = destinations.get(destinationName);
        if (ids == null) {
            return new ArrayList<>();
        }

        // Sorted ascending, matching the order messages were stored in
        return new ArrayList<>(ids);
    }

    private ConcurrentSkipListSet<Long> getIds(String destinationName) {
        return destinations.computeIfAbsent(destinationName, d -> new ConcurrentSkipListSet<>());
    }

    private static String toKey(String destinationName, Long messageId) {
        return destinationName + "/" + messageId;
    }

    public static void log(String s) {
        System.out.println("[MessageNoHeapDB] " + s);
    }

}
