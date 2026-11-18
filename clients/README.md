# EventPost clients

## License and Disclaimer

Copyright (c) 2019-2026 Eric J. Bruno, Allure Technology, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file or at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES (INCLUDING LOSS OR CORRUPTION OF DATA) OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

EventPost's original Java clients, the library they share, and a browser
page. They date from the first version of EventPost (then called
MessageServer) and have been brought up to date with it.

| Project | What it does |
|---|---|
| [SSELibrary](SSELibrary) | The client library the Java programs share: publish with `SSEDataPublisher`, receive with `SSEDataSubscriber` |
| [QueueSender](QueueSender) | Sends 1,000,000 messages to a queue from several threads, printing the rate |
| [QueueReceiver](QueueReceiver) | Receives from a queue, printing the rate |
| [TopicSender](TopicSender) | Publishes 10,000 messages to a topic |
| [TopicReceiver](TopicReceiver) | Prints each message from a topic |
| [TemperatureSender](TemperatureSender) | Publishes a simulated temperature reading four times a second |
| [thermometer](thermometer) | A web page that shows TemperatureSender's readings |

## Build

From this folder, build SSELibrary and the five Java programs in one step:

```sh
mvn package
```

The thermometer page needs no build.

## Try them

Start EventPost (see the [main README](../README.md#quick-start)), then run a
pair in two terminals:

| Demo | Start first | Then |
|---|---|---|
| Queue throughput | `cd QueueReceiver && ./run` | `cd QueueSender && ./run` |
| Topic | `cd TopicReceiver && ./run` | `cd TopicSender && ./run` |
| Live temperature | open the [thermometer](thermometer) page | `cd TemperatureSender && ./run` |

Each project's README covers its options and settings.

For new code, the dependency-free
[`EventPostClient.java`](../sample-apps/EventPostClient.java) in
`sample-apps` is a simpler starting point than SSELibrary.
