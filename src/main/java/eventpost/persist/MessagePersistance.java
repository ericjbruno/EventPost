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
