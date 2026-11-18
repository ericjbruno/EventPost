# SSELibrary

## License and Disclaimer

Copyright (c) 2019-2026 Eric J. Bruno, Allure Technology, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file or at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES (INCLUDING LOSS OR CORRUPTION OF DATA) OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

A small Java client library for [EventPost](https://github.com/ericjbruno/EventPost)
queues and topics. It publishes messages with an HTTP `POST` and receives them
by reading the server's stream of Server-Sent Events. It uses only the JDK and
has no dependencies.

The QueueSender, QueueReceiver, TopicSender, TopicReceiver and
TemperatureSender programs in this `clients` folder use it.

## Build

It needs Java 20 or later. `mvn package` in the `clients` folder builds it
along with the programs that use it. To use it from another project, install
it into your local Maven repository:

```sh
mvn install
```

Then add it to a project's `pom.xml`:

```xml
<dependency>
    <groupId>com.alluretechnology</groupId>
    <artifactId>SSELibrary</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Use

### Publish

`SSEDataPublisher.sendMessage` sends one message to a queue or topic. Pass the
full publish URL; the call returns once the server has accepted the message.

```java
SSEDataPublisher.sendMessage(
        "http://localhost:8080/eventpost/api/queue/publish?name=orders",
        "{\"order\": 1001}",
        null);
```

For a topic, use `/api/topic/publish?name=...` instead.

### Subscribe

Create an `SSEDataSubscriber` with the server's base address and the kind of
destination, then subscribe with an `SSECallback`. The subscriber runs on its
own thread and calls `onMessage` for each message.

```java
SSEDataSubscriber subscriber = new SSEDataSubscriber(
        "http://localhost:8080/eventpost",
        SSEDataSubscriber.DestinationType.QUEUE,
        null);

subscriber.subscribe("orders", (destination, data) ->
        System.out.println(destination + ": " + data));
```

Things to know:

- Each `SSEDataSubscriber` subscribes once, to one queue or topic. Create
  another for each additional destination.
- `onMessage` receives the whole SSE line, including its `data: ` prefix.
- The last argument, an authorization code, is sent in an
  `Authorization-Info` header. EventPost doesn't check it yet, so `null` is
  fine.

## Classes

| Class | Purpose |
|---|---|
| `SSEDataPublisher` | Sends a message to a queue or topic |
| `SSEDataSubscriber` | Receives a queue's or topic's messages on a background thread |
| `SSECallback` | The interface a subscriber implements to receive messages |
