import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Topic sample, part 1 of 2: a market data feed.
 *
 * Every second the ticker publishes the latest price of each stock to the
 * "stocks" topic. Every subscriber connected at that moment receives it,
 * whether that's one StockWatcher, ten, or the browser dashboard.
 *
 * A topic delivers the latest message to the subscribers that are listening
 * now; it doesn't store messages for later. So each message carries every
 * price, and a subscriber that misses one is fully up to date with the next.
 *
 * Run:  java StockTicker.java
 */
public class StockTicker {

    static final String TOPIC = "stocks";

    public static void main(String[] args) throws Exception {
        MessageServerClient client = new MessageServerClient();
        Random random = new Random();
        DateTimeFormatter clock = DateTimeFormatter.ofPattern("HH:mm:ss");

        Map<String, Double> prices = new LinkedHashMap<>();
        prices.put("AAPL", 228.50);
        prices.put("MSFT", 415.20);
        prices.put("NVDA", 138.75);
        prices.put("AMZN", 186.40);
        prices.put("ORCL", 172.10);

        System.out.println("Publishing prices to the '" + TOPIC + "' topic every second (Ctrl-C to stop)");

        while (true) {
            // Move each price up or down by as much as 1%
            prices.replaceAll((symbol, price) -> price * (1 + (random.nextDouble() - 0.5) / 50));

            // One JSON message with the time and every price, for example:
            // {"time":"14:03:05","AAPL":228.91,"MSFT":414.87,...}
            StringBuilder json = new StringBuilder("{\"time\":\"" + LocalTime.now().format(clock) + "\"");
            // Locale.ROOT so prices always use a decimal point, as JSON requires
            prices.forEach((symbol, price) -> json.append(String.format(Locale.ROOT, ",\"%s\":%.2f", symbol, price)));
            json.append("}");

            client.publish(MessageServerClient.Type.TOPIC, TOPIC, json.toString());
            System.out.println("Published " + json);

            Thread.sleep(1000);
        }
    }
}
