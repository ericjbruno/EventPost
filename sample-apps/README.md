# EventPost sample applications

Small, self-contained examples that show how to use EventPost's queues
and topics. Each Java program is a single source file that needs only the
JDK (Java 22 or later), with no build step and no dependencies. The web pages
are plain HTML and JavaScript.
`EventPostClient.java` is the shared client the Java programs use; it's
short and shows the whole protocol.

| Sample | Shows | Files |
|---|---|---|
| Order processing | **Queue**: each order goes to exactly one worker; orders wait while no worker is running | `OrderSubmitter.java`, `OrderWorker.java` |
| Live stock prices | **Topic**: every subscriber gets every update, in a terminal or a browser | `StockTicker.java`, `StockWatcher.java`, `stock-dashboard.html` |
| Match Center | **Both, in the browser and Java**: a scorekeeper publishes the match to a topic; fans send shout-outs to a queue | `match-center.html`, `MatchFeed.java` |

Start EventPost first, then run the samples from this folder:

```sh
# Queue: start two workers in separate terminals, then place orders
java OrderWorker.java Alice
java OrderWorker.java Bob
java OrderSubmitter.java 12

# Topic: start the ticker, then any number of watchers
java StockTicker.java
java StockWatcher.java
java StockWatcher.java AAPL NVDA
```

For the web pages, start the server from the project folder so it
serves this folder too, then open
<http://localhost:8080/samples/match-center.html> or
<http://localhost:8080/samples/stock-dashboard.html>:

```sh
java -Dmicronaut.router.static-resources.samples.paths=file:sample-apps \
     '-Dmicronaut.router.static-resources.samples.mapping=/samples/**' \
     -jar target/EventPost-1.0-SNAPSHOT.jar
```

See [Sample applications](../README.md#sample-applications) in the main
README for a walkthrough of each one.
