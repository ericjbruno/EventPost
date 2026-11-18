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

package test;

import sselibrary.SSECallback;
import sselibrary.SSEDataSubscriber;

/**
 * @author ericjbruno
 */
public class QueueReceiver implements SSECallback {

    SSEDataSubscriber sse = null;

    protected static String serverURI = "http://localhost:8080/eventpost";
    protected static String queueName = "queue1";

    String cloudAuthString = "test:welcome1";
    protected static String authCode = null;

    public boolean verbose = false;
    public int messageCount = 0;
    private long batchStartNanos = 0;

    public static void main(String[] args) {
        try {
            if ( args != null && args.length > 0 ) {
                queueName = args[0];
            }
            QueueReceiver receiver = new QueueReceiver(serverURI, "");
            receiver.subscribe(queueName, receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public QueueReceiver(String serverUrl, String authCode) throws Exception {
        sse = new SSEDataSubscriber(serverUrl,
                SSEDataSubscriber.DestinationType.QUEUE,
                authCode);
    }

    public void subscribe(String queueName, SSECallback callback) {
        sse.subscribe(queueName, callback);
    }

    @Override
    public void onMessage(String queue, String data) {
        if (verbose) {
            log("Message for destination '" + queue + "': " + data);
        }

        if (batchStartNanos == 0) {
            batchStartNanos = System.nanoTime();
        }

        if (++messageCount % 1000 == 0) {
            long now = System.nanoTime();
            double seconds = (now - batchStartNanos) / 1_000_000_000.0;
            double msgsPerSec = seconds > 0 ? 1000 / seconds : 0;
            batchStartNanos = now;
            log(String.format("1000 messages received for destination %s (%.1f msgs/sec)",
                    queue, msgsPerSec));
        }
    }

    public static void log(String s) {
        System.out.println("[QueueReceiver] " + s);
    }

}
