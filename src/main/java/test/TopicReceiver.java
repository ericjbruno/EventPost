package test;

import sselibrary.SSECallback;
import sselibrary.SSEDataSubscriber;


/**
 * @author ericjbruno
 */
public class TopicReceiver implements SSECallback {
	SSEDataSubscriber sse;
	
	protected static String serverURI = "http://localhost:8080/messageserver";
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
