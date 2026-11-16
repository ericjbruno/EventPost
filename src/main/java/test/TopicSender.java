package test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import sselibrary.SSEDataPublisher;

/**
 * @author ericjbruno
 */
public class TopicSender {

    protected static String eventPostURI = "http://localhost:8080/eventpost";
    protected static String topicName = "topic1";
    protected static SSEDataPublisher dataPublisher = null;

    String cloudAuthString = "test:welcome1";

    protected static String authCode = null;

    Thread CANDataListener = null;
    boolean listening = true;

    //////////////////////////////////////////////////////////////////////////
    public static void main(String[] args) {
        // Read in CAN sender address and port, and the SSE server address and port
        //
        TopicSender.loadConfigs();

        try {
            TopicSender sender = new TopicSender();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TopicSender() throws Exception {
        dataPublisher = new SSEDataPublisher();
        log("SSE Data Publisher Started");

        // Generate fake test data
        //
        new Thread() {
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    try {
                        TopicSender.sendToServer("Message " + i);
                        //sleep(1000);
                    }
                    catch (Exception e) {
                    }
                }
            }
        }.start();
    }

    protected static void loadConfigs() {
        // Attempt to load a properties file if found
        //
        Properties props = new Properties();

        try ( FileInputStream in
                = new FileInputStream(
                        new File("./sender.properties"))) {

            props.load(in);

            TopicSender.eventPostURI
                    = props.getProperty("EventPostURI",
                            props.getProperty("MessageServerURI",   // the name before EventPost
                            TopicSender.eventPostURI));

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        log("-----------------------------");
        log("Using EventPostURI=" + eventPostURI);
        log("-----------------------------");
    }

    public boolean connectToServer() {
        return false;
    }

    public static void sendToServer(String data) throws Exception {
        String url = eventPostURI + "/api/topic/publish?name=" + topicName;

        if (dataPublisher != null) {
            dataPublisher.sendMessage(url, data, authCode);
        }
    }

    public static void log(String s) {
        System.out.println("[TopicSender] " + s);
    }
}
