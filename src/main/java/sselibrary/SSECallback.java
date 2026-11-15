package sselibrary;

/**
 * @author ericjbruno
 */
public interface SSECallback {
	public void onMessage(String topic, String data);
}
