# QueueSender

## License and Disclaimer

Copyright (c) 2019-2026 Eric J. Bruno, Allure Technology, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file or at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES (INCLUDING LOSS OR CORRUPTION OF DATA) OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

A load generator for [EventPost](https://github.com/ericjbruno/EventPost) queues. It sends 1,000,000 messages, `Message 0`
through `Message 999999`, to the `queue1` queue, spread across several sender
threads, and prints the send rate after every 1,000 messages.

Run [QueueReceiver](../QueueReceiver) at the same time to take the messages off
the queue. Messages that no receiver takes wait in the queue.

## Build

It uses SSELibrary, which isn't on Maven Central. Install it into your local
Maven repository first, then build this project:

```sh
(cd ../SSELibrary && mvn install)
mvn package
```

## Run

Start EventPost, then:

```sh
./run                                                # 8 threads, queue1
java -jar target/QueueSender-1.0-SNAPSHOT.jar 16 orders   # 16 threads, "orders"
```

The optional arguments are the number of sender threads and the queue name.

## Settings

`sender.properties` in the current folder is read at startup:

| Property | Default | Meaning |
|---|---|---|
| `EventPostURI` | `http://localhost:8080/eventpost` | The server's address. The old name, `MessageServerURI`, also works |
| `SenderThreads` | `8` | Number of sender threads. When set, it overrides the first argument; the shipped file doesn't set it |

Messages from parallel threads can be queued in any order. Use one thread if
order matters.
