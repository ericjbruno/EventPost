# QueueReceiver

## License and Disclaimer

Copyright (c) 2019-2026 Eric J. Bruno, Allure Technology, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file or at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES (INCLUDING LOSS OR CORRUPTION OF DATA) OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

Receives messages from an [EventPost](https://github.com/ericjbruno/EventPost) queue and prints the receive rate after every
1,000 messages. It's the receiving half of [QueueSender](../QueueSender).

Each queue message goes to only one receiver, so run two or more
QueueReceivers to see the messages shared out between them.

## Build

It uses [SSELibrary](../SSELibrary), which is in the same `clients` folder.
Build everything there at once:

```sh
cd ..          # the clients folder
mvn package
```

To build just this project, install SSELibrary into your local Maven
repository first:

```sh
(cd ../SSELibrary && mvn install)
mvn package
```

## Run

Start EventPost, then:

```sh
./run                                                  # queue1
java -jar target/QueueReceiver-1.0-SNAPSHOT.jar orders     # another queue
```

The optional argument is the queue name. The receiver keeps running until
you stop it with Ctrl-C.

## Settings

The server address, `http://localhost:8080/eventpost`, is set by `serverURI`
in [QueueReceiver.java](src/main/java/test/QueueReceiver.java).
