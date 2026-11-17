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

package eventpost.sse;

/**
 * @author ericjbruno
 */
public class Topic extends Destination {

    protected Message lastMessage = null;
    final protected Object topicMonitor = new Object();

    public Topic(String name) {
        super(name);
    }

    public Message getNextMessage() throws InterruptedException {
        synchronized (topicMonitor) {
            topicMonitor.wait();
        }

        return lastMessage;
    }

    public boolean addMessage(String msgData) {
        Long messageId = System.currentTimeMillis();
        Message msg = new Message(messageId, msgData);
        lastMessage = msg;

        // Notify ALL listeners of the message
        synchronized (topicMonitor) {
            topicMonitor.notifyAll();
        }

        return true;
    }
}
