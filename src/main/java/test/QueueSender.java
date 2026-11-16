package test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import sselibrary.SSEDataPublisher;

/**
 * @author ericjbruno
 */
public class QueueSender {
	protected static String eventPostURI = "http://localhost:8080/eventpost";
	protected static String queueName = "queue1";
	protected static SSEDataPublisher dataPublisher = null;
	
    String cloudAuthString = "test:welcome1";
    
	protected static String authCode = null;

	protected static int senderThreads = 8;
	protected static int totalMessages = 1000000;

	Thread CANDataListener = null;
	boolean listening = true;
	
	//////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		// Optional first argument overrides the number of sender threads
		if ( args.length > 0 ) {
			senderThreads = Integer.parseInt(args[0]);
			if ( args.length > 1 ) {
				queueName = args[1];
			}
		}
		
		// Read in CAN sender address and port, and the SSE server address and port
		//
		QueueSender.loadConfigs();

		try {
			QueueSender sender = new QueueSender();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public QueueSender() throws Exception {
		// HttpURLConnection only keeps 5 idle connections per server by default,
		// so raise it to one per thread or the extra connections get re-opened
		// on every send
		System.setProperty("http.maxConnections", String.valueOf(senderThreads));

		log("Sending " + totalMessages + " messages using " + senderThreads + " threads");

		// Generate fake test data, sending in parallel across several threads
		//
		AtomicInteger nextMessage = new AtomicInteger(0);
		AtomicInteger sentCount = new AtomicInteger(0);
		AtomicLong batchStartNanos = new AtomicLong(System.nanoTime());

		for ( int t = 0; t < senderThreads; t++ ) {
			new Thread() {
				public void run() {
					int i;
					while ( (i = nextMessage.getAndIncrement()) < totalMessages ) {
						try {
							QueueSender.sendToServer("Message " + i);

							if ( sentCount.incrementAndGet() % 1000 == 0 ) {
								long now = System.nanoTime();
								long start = batchStartNanos.getAndSet(now);
								double seconds = (now - start) / 1_000_000_000.0;
								double msgsPerSec = seconds > 0 ? 1000 / seconds : 0;
								log(String.format("1000 messages sent to destination %s (%.1f msgs/sec)",
										queueName, msgsPerSec));
							}
						}
						catch ( Exception e ) { 
	                        e.printStackTrace();
	                    }
					}
				}
			}.start();
		}
	}
	
	protected static void loadConfigs() {
		// Attempt to load a properties file if found
		//
		Properties props = new Properties();

		try ( FileInputStream in = 
				new FileInputStream(
						new File("./sender.properties")) ) {

			props.load(in);

			QueueSender.eventPostURI = 
					props.getProperty("EventPostURI",
										props.getProperty("MessageServerURI",   // the name before EventPost
										QueueSender.eventPostURI));
			QueueSender.senderThreads = 
					Integer.parseInt(props.getProperty("SenderThreads", 
										String.valueOf(QueueSender.senderThreads)));

		}
		catch ( Exception e ) {
			e.printStackTrace();
		}

		url = eventPostURI + "/api/queue/publish?name=" + queueName;

		log("-----------------------------");
		log("Using EventPostURI="+eventPostURI);
		log("Using SenderThreads="+senderThreads);
		log("-----------------------------");
	}
	
	public boolean connectToServer() {
		return false;
	}
	
	static String url = eventPostURI + "/api/queue/publish?name=" + queueName;
	public static void sendToServer(String data) throws Exception {
        SSEDataPublisher.sendMessage(url, data, authCode);
	}
	
    public static void log(String s) {
        System.out.println("[QueueSender] " + s);
    }
}
