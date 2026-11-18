# TemperatureSender

## License and Disclaimer

Copyright (c) 2019-2026 Eric J. Bruno, Allure Technology, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file or at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES (INCLUDING LOSS OR CORRUPTION OF DATA) OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

Simulates a temperature sensor. Every quarter second it publishes a reading,
in degrees Fahrenheit, to the `temp1` topic on an [EventPost](https://github.com/ericjbruno/EventPost) server. The reading
starts at 100 and drifts up or down by one degree at a time, staying between
50 and 150.

The [thermometer](../thermometer) web page displays the readings.

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
./run
```

It keeps sending until you stop it with Ctrl-C.

## Settings

`sender.properties` in the current folder is read at startup:

| Property | Default | Meaning |
|---|---|---|
| `EventPostURI` | `http://localhost:8080/eventpost` | The server's address. The old name, `MessageServerURI`, also works |
