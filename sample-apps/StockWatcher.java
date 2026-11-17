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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Topic sample, part 2 of 2: a trader's price monitor.
 *
 * The watcher subscribes to the "stocks" topic and prints each update, with
 * an arrow showing which way each price moved. Start several watchers in
 * separate terminals: unlike a queue, every one of them gets every update.
 * Give stock symbols as arguments to watch only those.
 *
 * Run:  java StockWatcher.java [SYMBOL ...]     (for example: java StockWatcher.java AAPL NVDA)
 */
public class StockWatcher {

    static final String TOPIC = "stocks";

    // Matches "SYMBOL":price pairs in the ticker's JSON message
    static final Pattern PRICE = Pattern.compile("\"([A-Z]+)\":([0-9.]+)");
    static final Pattern TIME = Pattern.compile("\"time\":\"([^\"]+)\"");

    public static void main(String[] args) throws Exception {
        Set<String> watchList = Set.of(args);
        Map<String, Double> lastPrices = new HashMap<>();
        EventPostClient client = new EventPostClient();

        System.out.println("Watching the '" + TOPIC + "' topic"
                + (watchList.isEmpty() ? "" : " for " + String.join(", ", watchList)) + "...");

        client.subscribe(EventPostClient.Type.TOPIC, TOPIC, update -> {
            Matcher time = TIME.matcher(update);
            StringBuilder line = new StringBuilder(time.find() ? time.group(1) : "");

            Matcher m = PRICE.matcher(update);
            while (m.find()) {
                String symbol = m.group(1);
                double price = Double.parseDouble(m.group(2));
                if (!watchList.isEmpty() && !watchList.contains(symbol)) {
                    continue;
                }

                Double last = lastPrices.put(symbol, price);
                String arrow = (last == null || price == last) ? " " : (price > last ? "▲" : "▼");
                line.append(String.format("   %s %8.2f %s", symbol, price, arrow));
            }

            System.out.println(line);
        });
    }
}
