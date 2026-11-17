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

import java.util.Random;

/**
 * Queue sample, part 1 of 2: an online store placing orders.
 *
 * Each order is sent to the "orders" queue, where it waits until an
 * OrderWorker takes it. Every order is delivered to exactly one worker, so
 * running several workers shares the load between them, and orders placed
 * while no worker is running are kept until one starts.
 *
 * Run:  java OrderSubmitter.java [number-of-orders]     (default 10)
 */
public class OrderSubmitter {

    static final String QUEUE = "orders";

    static final String[] CUSTOMERS = { "Ada", "Grace", "Linus", "Margaret", "Alan", "Barbara" };
    static final String[] PRODUCTS  = { "Keyboard", "Monitor", "Headphones", "Webcam", "Laptop stand", "USB hub" };

    public static void main(String[] args) throws Exception {
        int count = args.length > 0 ? Integer.parseInt(args[0]) : 10;
        EventPostClient client = new EventPostClient();
        Random random = new Random();

        for (int i = 1; i <= count; i++) {
            String orderId = String.format("A-%04d", 1000 + i);
            String customer = CUSTOMERS[random.nextInt(CUSTOMERS.length)];
            String product = PRODUCTS[random.nextInt(PRODUCTS.length)];
            int quantity = 1 + random.nextInt(5);

            // A message is one line of text; JSON is a natural choice
            String order = String.format(
                    "{\"orderId\":\"%s\",\"customer\":\"%s\",\"product\":\"%s\",\"quantity\":%d}",
                    orderId, customer, product, quantity);

            client.publish(EventPostClient.Type.QUEUE, QUEUE, order);
            System.out.printf("Placed order %s: %d x %s for %s%n", orderId, quantity, product, customer);
        }

        System.out.println("Placed " + count + (count == 1 ? " order" : " orders")
                + " on the '" + QUEUE + "' queue");
    }
}
