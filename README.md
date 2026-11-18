<img src="site/eventpost-icon.svg" alt="EventPost icon: a mailbox with its flag up, sending out events" width="112" align="right">

# EventPost

## License and Disclaimer

Copyright (c) 2019-2026 Eric J. Bruno, Allure Technology, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file or at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES (INCLUDING LOSS OR CORRUPTION OF DATA) OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

EventPost is a lightweight message server for queues and publish/subscribe
topics, built on [Micronaut](https://micronaut.io). Clients talk to it over
plain HTTP: they publish with a `POST` and receive messages as a stream of
[Server-Sent Events](https://html.spec.whatwg.org/multipage/server-sent-events.html)
(SSE). That means any language with an HTTP client can use it, and so can a
web page, with the browser's built-in `EventSource`. There's no client library
to install and no binary protocol to learn.

Queue messages are persisted, either off-heap in
[NoHeapDB](https://github.com/ericjbruno/NoHeapDB) (the default) or in an
Oracle NoSQL Database KV store.

## What it's useful for

- **Work queues and background jobs.** Hand off orders, emails, image
  processing or reports to one or more workers. Each job goes to exactly one
  worker, and jobs wait in the queue while no worker is running.
- **Decoupling services.** A producer doesn't need to know who consumes its
  messages, or whether they're running right now.
- **Live updates.** Push prices, sensor readings, scores, build status or
  dashboard metrics to every connected client the moment they change,
  including straight to a browser.
- **Lightweight integration.** Connect scripts, services and web pages written
  in different languages through a few HTTP calls, with no broker client
  library on either side.

## Queues and topics

| | Queue | Topic |
|---|---|---|
| Pattern | Point-to-point | Publish/subscribe |
| Who receives a message | Exactly one connected receiver | Every connected subscriber |
| With several receivers | Messages are distributed between them | Each one gets every update |
| No receiver connected | Messages wait in the queue | Messages aren't kept |
| Persisted | Yes (see [Persistence](#persistence)) | No |
| Best for | Jobs and commands that must each be handled once | Current state and live notifications |

Topics deliver the **latest** message. A subscriber that is still receiving
one message when the next two arrive gets only the newest of them, and a
subscriber that connects later sees only messages published after it
connected. That suits live state, such as a price or a temperature, where the
newest value is what matters, so design topic messages to carry the full
current state rather than a change. Use a queue when every message must be
handled.

## Quick start

### Prerequisites

- **JDK 26**, for building and running the server. The sample applications
  need Java 22 or later.
- **Maven 3.9** or later.
- **NoHeapDB** in your local Maven repository. It isn't on Maven Central:

  ```sh
  git clone https://github.com/ericjbruno/NoHeapDB.git
  cd NoHeapDB && mvn install -DskipTests
  ```

- **The Oracle NoSQL client jar** (`kvclient` 25.3.21) in your local Maven
  repository. The build needs it even if you only use NoHeapDB. It ships in
  the `lib` folder of the
  [Oracle NoSQL Database](https://www.oracle.com/database/nosql/) download:

  ```sh
  mvn install:install-file -Dfile=$HOME/Software/kv-25.3.21/lib/kvclient-25.3.21.jar \
      -DgroupId=com.oracle.nosql -DartifactId=kvclient -Dversion=25.3.21 \
      -Dpackaging=jar -DgeneratePom=true
  ```

### Build and start

```sh
mvn package
java -jar target/EventPost-1.0-SNAPSHOT.jar      # or ./run
```

The server listens on port 8080, and every endpoint is under
`http://localhost:8080/eventpost/api`. By default it stores queue
messages in memory with NoHeapDB, so it needs no database running. On
startup it prints the storage it's using:

```
[MessageNoHeapDB] Using IN_MEMORY store
[MessagePersistanceFactory] Using message persistence: noheapdb
```

### Try it with curl

In one terminal, listen on a queue named `greetings`:

```sh
curl -N http://localhost:8080/eventpost/api/queue/greetings
```

In another, send it a message:

```sh
curl -H "Content-Type: text/event-stream" \
     --data-binary $'data: Hello, EventPost!\n\n' \
     "http://localhost:8080/eventpost/api/queue/publish?name=greetings"
```

The listener prints `data: Hello, EventPost!`. Run the publish command
again with no listener connected, then start the listener: the message was
waiting in the queue. Replace `queue` with `topic` in both URLs to try a
topic, and notice that topic messages sent with no subscriber connected are
not kept.

## Sample applications

The [`sample-apps`](sample-apps) folder has small, realistic examples: an
order-processing queue and a live stock-price topic in Java, and a Match
Center web app that uses both from the browser. Each Java program is a single
source file that needs only the JDK: run it with `java File.java`, with no
build step and no dependencies. Java 22 or later compiles the shared
[`EventPostClient.java`](sample-apps/EventPostClient.java) from the
same folder automatically. That client is under 100 lines, most of them
comments, and shows the whole protocol; read it first.

Start EventPost, then run the samples from the `sample-apps` folder. To
use a server somewhere other than `localhost:8080`, set `EVENTPOST_URL`,
for example `EVENTPOST_URL=http://myhost:9090/eventpost/api`.

### Order processing (queue)

An online store places orders on the `orders` queue, and warehouse workers
pack and ship them.

- [`OrderSubmitter.java`](sample-apps/OrderSubmitter.java) places orders, each
  a one-line JSON message such as
  `{"orderId":"A-1001","customer":"Ada","product":"Monitor","quantity":2}`.
- [`OrderWorker.java`](sample-apps/OrderWorker.java) receives orders and
  "packs" each one, taking longer for bigger orders.

Start two workers in separate terminals, then place some orders:

```sh
java OrderWorker.java Alice
java OrderWorker.java Bob
java OrderSubmitter.java 12
```

Each order is handled by exactly one worker, and the server shares them out
between the workers:

```
[Alice] Packing order A-1002: 3 x Laptop stand for Barbara...
[Alice] Shipped order A-1002
[Alice] Packing order A-1004: 2 x Keyboard for Margaret...
```
```
[Bob] Packing order A-1001: 3 x Webcam for Linus...
[Bob] Shipped order A-1001
[Bob] Packing order A-1003: 2 x Webcam for Barbara...
```

Now stop both workers, run `java OrderSubmitter.java 3`, and start a worker
again. It picks up the three orders that were waiting. With
[persisted storage](#persistence), they would also survive a server restart.

### Live stock prices (topic)

A market data feed publishes prices to the `stocks` topic every second, and
any number of subscribers display them.

- [`StockTicker.java`](sample-apps/StockTicker.java) publishes one message
  per second with every price, for example
  `{"time":"14:03:05","AAPL":228.91,"MSFT":414.87,"NVDA":138.20,...}`.
- [`StockWatcher.java`](sample-apps/StockWatcher.java) prints each update in
  the terminal, with arrows showing which way each price moved. Give symbols
  as arguments to watch only those.
- [`stock-dashboard.html`](sample-apps/stock-dashboard.html) is a live web
  page with a price table and a sparkline for each stock, using only the
  browser's `EventSource`.

```sh
java StockTicker.java
java StockWatcher.java
java StockWatcher.java AAPL NVDA
```

Unlike a queue, every watcher receives every update:

```
09:33:22   AAPL   229.19 ▼   MSFT   420.36 ▲   NVDA   138.61 ▲   AMZN   190.22 ▼   ORCL   167.97 ▼
09:33:23   AAPL   229.90 ▲   MSFT   416.39 ▼   NVDA   137.84 ▼   AMZN   190.63 ▲   ORCL   167.73 ▼
```

Each message carries every price, so a watcher that misses an update, or
starts late, is fully up to date with the next one. That's the right way to
design messages for a topic.

To open the browser dashboard, start the server so it
[also serves the sample pages](#serving-the-browser-samples), open
<http://localhost:8080/samples/stock-dashboard.html>, and start
`StockTicker.java`.

### Match Center (browser app, queue and topic)

[`match-center.html`](sample-apps/match-center.html) is a live match
scoreboard written in plain JavaScript, with no libraries and no server-side
code. It shows that a browser can publish as well as receive, and that one
application can use a topic and a queue together:

- The **Scorekeeper** view runs the match: team names and colours, goals,
  cards, commentary, the period and the game clock. It publishes the full
  match state to the `match` topic, every time something changes and once a
  second while the clock runs, so a scoreboard opened mid-match catches up
  within a few seconds.
- The **Scoreboard** view is a stadium-style live display. Every open
  scoreboard receives every update from the topic.
- Fans send **shout-outs** from the Scoreboard view to the `shoutouts` queue.
  Each one goes to just one scorekeeper, and they wait in the queue while no
  scorekeeper is open. The scorekeeper can feature one, and it
  appears on every scoreboard.

Start the server so it [serves the sample pages](#serving-the-browser-samples),
then open the Scorekeeper in one window and a Scoreboard in one or more
others:

- <http://localhost:8080/samples/match-center.html#scorekeeper>
- <http://localhost:8080/samples/match-center.html>

[`MatchFeed.java`](sample-apps/MatchFeed.java) follows the same match from
Java, printing each new event and featured shout-out as it happens. Run
`java MatchFeed.java` from the `sample-apps` folder alongside the browsers:

```
24'  ⚽ GOAL! Lions  1–0    Lions 1 - Falcons 0
53'  ⚽ GOAL! Falcons  1–1    Lions 1 - Falcons 1
    📣 Ada: What a strike! Lions all the way 🦁
```

Publishing from JavaScript is a single `fetch()` call, and receiving is an
`EventSource`, as the two small functions at the top of the page's script
show. Fan names and messages are displayed with `textContent`, never
`innerHTML`, so a shout-out can't inject markup into other people's pages.

Open only one Scorekeeper at a time: two would each publish their own match
state, and the shout-outs would be shared between them.

### Serving the browser samples

EventPost can serve the `sample-apps` folder itself, so the pages and the
API share an origin and need no CORS setting or separate web server. From the
project folder:

```sh
java -Dmicronaut.router.static-resources.samples.paths=file:sample-apps \
     '-Dmicronaut.router.static-resources.samples.mapping=/samples/**' \
     -jar target/EventPost-1.0-SNAPSHOT.jar
```

The pages are then at `http://localhost:8080/samples/`. To use a page with a
server elsewhere, add `?server=http://myhost:9090` to its URL; that server
needs [CORS enabled](#browser-clients-and-cors).

## Original clients

The [`clients`](clients) folder holds EventPost's original Java clients and
the library they share. They're full Maven projects, unlike the single-file
samples above, and QueueSender is the load generator behind the numbers in
[Performance](#performance).

| Project | What it does |
|---|---|
| [SSELibrary](clients/SSELibrary) | The client library the Java programs share |
| [QueueSender](clients/QueueSender) | Sends 1,000,000 messages to a queue from several threads, printing the rate |
| [QueueReceiver](clients/QueueReceiver) | Receives from a queue, printing the rate |
| [TopicSender](clients/TopicSender) | Publishes 10,000 messages to a topic |
| [TopicReceiver](clients/TopicReceiver) | Prints each message from a topic |
| [TemperatureSender](clients/TemperatureSender) | Publishes a simulated temperature reading four times a second |
| [thermometer](clients/thermometer) | A web page that shows TemperatureSender's readings |

Build them all from the `clients` folder:

```sh
cd clients && mvn package
```

Each project's README explains how to run it.

## HTTP API

All paths are relative to `http://<host>:8080/eventpost/api`. Queue and
topic names are created on first use. The same endpoints are also served
under `/messageserver/api`, the path used before the project was renamed
EventPost, so existing clients keep working.

| Action | Request |
|---|---|
| Send to a queue | `POST /queue/publish?name={queue}` |
| Receive from a queue | `GET /queue/{queue}` |
| Publish to a topic | `POST /topic/publish?name={topic}` |
| Subscribe to a topic | `GET /topic/{topic}` |

### Publishing

Send the message in Server-Sent Events format, with the
`text/event-stream` content type:

```http
POST /eventpost/api/queue/publish?name=orders HTTP/1.1
Content-Type: text/event-stream

data: {"orderId":"A-1001","quantity":2}

```

- A message is one line of text. JSON objects work well. Leading and
  trailing spaces are trimmed.
- Each `data:` line in the body is a separate message, so you can send a
  batch in one request. For topics, only the last message of a batch reaches
  subscribers, since topics deliver the latest message.
- Other SSE lines, such as `event:` or `id:`, are ignored.
- Responses: `200` with the destination name when the messages are stored,
  `500` if a message couldn't be stored, `401` if the `name` parameter is
  missing, and `415` for any content type other than `text/event-stream`.

### Receiving

`GET /queue/{name}` and `GET /topic/{name}` return a Server-Sent Events
stream that stays open. Each message arrives as a `data:` line followed by a
blank line:

```
data: {"orderId":"A-1001","quantity":2}

```

From Java, read the response line by line, as `EventPostClient` does with
`java.net.http.HttpClient`. From a browser, use `EventSource`:

```js
const events = new EventSource("http://localhost:8080/eventpost/api/topic/stocks");
events.onmessage = (event) => console.log(JSON.parse(event.data));
```

## Configuration

Settings can be given as Java system properties (`-D...` before `-jar`) or
as environment variables. The names from before the project was renamed
EventPost, such as `messageserver.persistence` and
`MESSAGESERVER_PERSISTENCE`, still work; the new names take precedence.

### Persistence

| Setting | Environment variable | Values |
|---|---|---|
| `eventpost.persistence` | `EVENTPOST_PERSISTENCE` | `noheapdb` (default) or `oraclekv` |
| `eventpost.noheapdb.storage` | `EVENTPOST_NOHEAPDB_STORAGE` | `in_memory` (default) or `persisted` |

- **NoHeapDB, in memory (default).** Messages are kept off the Java heap, so
  large backlogs add no garbage collection pressure. They're lost when the
  server stops. Good for development, testing and messages that can be
  regenerated.
- **NoHeapDB, persisted.** Messages are kept in memory-mapped files under
  `~/JavaOffHeap` and reloaded when the server restarts. Stop the server
  normally (Ctrl-C or `kill`) so it closes the store cleanly; after a crash,
  NoHeapDB rebuilds its index from the stored messages on the next start.

  ```sh
  java -Deventpost.noheapdb.storage=persisted -jar target/EventPost-1.0-SNAPSHOT.jar
  ```

- **Oracle NoSQL Database.** Messages are stored in an Oracle NoSQL KV store,
  which must be running at `127.0.0.1:5001` with the store name `kvstore`.

  ```sh
  java -Deventpost.persistence=oraclekv -jar target/EventPost-1.0-SNAPSHOT.jar
  ```

The storage is set up before the server starts, so an invalid setting or an
unreachable database stops it immediately with an error, rather than at the
first message. Only the selected storage is loaded, so NoHeapDB mode doesn't
need Oracle NoSQL running.

### Port

Use any Micronaut setting the same way. For example, to listen on port 9090:

```sh
java -Dmicronaut.server.port=9090 -jar target/EventPost-1.0-SNAPSHOT.jar
```

### Browser clients and CORS

A web page served from a different origin than the server, such as
`http://localhost:8000`, can only connect if CORS is enabled. Pages that
EventPost serves itself, like the
[browser samples](#serving-the-browser-samples), don't need it.

```sh
java -Dmicronaut.server.cors.enabled=true -jar target/EventPost-1.0-SNAPSHOT.jar
```

This allows requests from any web origin. Pages must be served over HTTP:
browsers send `Origin: null` from a `file://` page, which the server refuses.
See the Micronaut
[CORS documentation](https://docs.micronaut.io/latest/guide/#cors) to limit
the allowed origins.

## Performance

A sender that waits for each response before sending the next message is
limited by the HTTP round trip, at around 750 messages per second on a single
machine. Throughput grows with the number of concurrent senders: in local
tests, 16 parallel senders reached about 12,000 messages per second, with
NoHeapDB and with Oracle NoSQL, and every message was delivered. Batching
several `data:` lines into one request also helps.

To repeat the test, start [QueueReceiver](clients/QueueReceiver), then
[QueueSender](clients/QueueSender) with 16 threads:

```sh
cd clients/QueueReceiver && ./run
cd clients/QueueSender && java -jar target/QueueSender-1.0-SNAPSHOT.jar 16
```

Messages from parallel senders can be stored in any order. Messages sent one
after another by the same sender reach a single receiver in that order.

## Limitations

- **No authentication.** Any client that can reach the server can publish
  and receive. Run it on a trusted network, or behind a proxy that handles
  access control.
- **Queue delivery isn't acknowledged.** A queue message is removed once it's
  written to a receiver's connection. A receiver that disconnects (a closed
  browser tab, a reloaded page, a stopped program) loses nothing: the server
  notices and keeps undelivered messages in the queue for the next receiver.
  But if a receiver crashes, the message it was handling, and any already
  sent to it that it hadn't read yet, are lost.
- **Queues share messages out as they arrive,** alternating between connected
  receivers, rather than waiting for a receiver to finish its last message.
- **Topics aren't durable** and deliver the latest message, as described in
  [Queues and topics](#queues-and-topics).

## Project structure

```
EventPost/
├── src/main/java/eventpost/
│   ├── EventPost.java              Application entry point
│   ├── QueueController.java        Queue endpoints
│   ├── TopicController.java        Topic endpoints
│   ├── Settings.java               Reads settings (new and old names)
│   ├── sse/                        Queues, topics and message handling
│   └── persist/                    Persistence: NoHeapDB and Oracle NoSQL
├── sample-apps/                    Sample applications
├── clients/                        Original Java clients, SSELibrary, thermometer page
├── article/                        The article (ARTICLE.md) and its diagrams
├── site/EventPost.html             Single-page website for EventPost
├── LICENSE                         Apache License 2.0
├── pom.xml
└── run                             Starts the server
```
