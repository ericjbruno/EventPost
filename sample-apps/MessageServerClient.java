import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A minimal MessageServer client used by the sample applications. It needs
 * nothing but the JDK, and shows the whole protocol:
 *
 *   Publish:   POST {server}/queue/publish?name={queue}   (or /topic/publish)
 *              Content-Type: text/event-stream
 *              Body: "data: {message}\n\n"
 *
 *   Subscribe: GET {server}/queue/{queue}                 (or /topic/{topic})
 *              The response is a Server-Sent Events stream; each message
 *              arrives as a "data: {message}" line.
 *
 * The server address defaults to http://localhost:8080/messageserver/api and
 * can be changed with the MESSAGESERVER_URL environment variable.
 */
public class MessageServerClient {

    public enum Type { QUEUE, TOPIC }

    private static final String DEFAULT_URL = "http://localhost:8080/messageserver/api";

    private final String serverUrl;
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public MessageServerClient() {
        String url = System.getenv("MESSAGESERVER_URL");
        this.serverUrl = (url == null || url.isBlank()) ? DEFAULT_URL : url;
    }

    /**
     * Sends one message to a queue or topic. A message is a single line of
     * text, such as a JSON object.
     */
    public void publish(Type type, String name, String message) throws Exception {
        if (message.contains("\n") || message.contains("\r")) {
            throw new IllegalArgumentException("A message must be a single line");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/" + path(type) + "/publish?name=" + encode(name)))
                .header("Content-Type", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString("data: " + message + "\n\n"))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Publish failed: HTTP " + response.statusCode()
                    + " " + response.body());
        }
    }

    /**
     * Subscribes to a queue or topic and calls the handler with each message
     * as it arrives. This blocks for as long as the connection stays open.
     */
    public void subscribe(Type type, String name, Consumer<String> handler) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + "/" + path(type) + "/" + encode(name)))
                .header("Accept", "text/event-stream")
                .GET()
                .build();

        HttpResponse<Stream<String>> response = http.send(request, HttpResponse.BodyHandlers.ofLines());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Subscribe failed: HTTP " + response.statusCode());
        }

        try (Stream<String> lines = response.body()) {
            lines.filter(line -> line.startsWith("data:"))
                 .map(line -> line.substring("data:".length()).trim())
                 .forEach(handler);
        }
    }

    private static String path(Type type) {
        return type == Type.QUEUE ? "queue" : "topic";
    }

    private static String encode(String name) {
        return URLEncoder.encode(name, StandardCharsets.UTF_8);
    }
}
