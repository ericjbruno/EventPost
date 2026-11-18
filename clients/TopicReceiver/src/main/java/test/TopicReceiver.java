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
public class TopicReceiver implements SSECallback {
	SSEDataSubscriber sse;
	
	protected static String serverURI = "http://localhost:8080/eventpost";
	protected static String topicName = "topic1";
	
	String cloudAuthString = "test:welcome1";
	protected static String authCode = null;

	public static void main(String[] args) {
        //Micronaut.run(TopicReceiver.class, "micronaut.server.port", "8181" );
		
		try {
			TopicReceiver receiver = new TopicReceiver(serverURI, "");
			receiver.subscribe(topicName, receiver);
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public TopicReceiver(String serverUrl, String authCode) throws Exception {
		sse = new SSEDataSubscriber(serverUrl, 
									SSEDataSubscriber.DestinationType.TOPIC,
									authCode);
	}
	
	public void subscribe(String topicName, SSECallback callback) {
		sse.subscribe( topicName, callback );
	}

	@Override
	public void onMessage(String topic, String data) {
		log("Message for destination '" + topic + "': " + data);
	}
	
    public static void log(String s) {
        System.out.println("[TopicReceiver] " + s);
    }

}
