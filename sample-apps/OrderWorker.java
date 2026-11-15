import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Queue sample, part 2 of 2: a warehouse worker that packs and ships orders.
 *
 * The worker listens on the "orders" queue and handles each order it
 * receives. Start two or three workers in separate terminals and each order
 * goes to only one of them. Stop them all, place more orders, and start a
 * worker again: the orders were waiting in the queue.
 *
 * Run:  java OrderWorker.java [worker-name]     (default "Worker")
 */
public class OrderWorker {

    static final String QUEUE = "orders";

    public static void main(String[] args) throws Exception {
        String workerName = args.length > 0 ? args[0] : "Worker";
        MessageServerClient client = new MessageServerClient();

        System.out.println(workerName + " is waiting for orders on the '" + QUEUE + "' queue...");

        // Blocks, calling handleOrder() for each order this worker receives
        client.subscribe(MessageServerClient.Type.QUEUE, QUEUE, order -> handleOrder(workerName, order));
    }

    static void handleOrder(String workerName, String order) {
        String orderId = field(order, "orderId");
        String customer = field(order, "customer");
        String product = field(order, "product");
        int quantity = Integer.parseInt(field(order, "quantity"));

        System.out.printf("[%s] Packing order %s: %d x %s for %s...%n",
                workerName, orderId, quantity, product, customer);

        // Pretend to do the work: bigger orders take longer to pack
        sleep(300L * quantity);

        System.out.printf("[%s] Shipped order %s%n", workerName, orderId);
    }

    // Reads one field from a flat JSON object. Enough for this sample; a real
    // application would use a JSON library
    static String field(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\":\"?([^\",}]*)").matcher(json);
        return m.find() ? m.group(1) : "";
    }

    static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
