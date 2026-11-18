# thermometer

## License and Disclaimer

Copyright (c) 2019-2026 Eric J. Bruno, Allure Technology, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License in the [LICENSE](LICENSE) file or at <https://www.apache.org/licenses/LICENSE-2.0>.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES (INCLUDING LOSS OR CORRUPTION OF DATA) OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

A web page that shows a live temperature reading. It subscribes to the
`temp1` topic on an [EventPost](https://github.com/ericjbruno/EventPost) server with the browser's built-in `EventSource`
and updates the display with each new reading. It needs no JavaScript
library and no build step.

[TemperatureSender](../TemperatureSender) publishes the readings.

## Run

Let EventPost serve the page, so the page and the server share an origin.
From the EventPost folder:

```sh
java -Dmicronaut.router.static-resources.thermometer.paths=file:../thermometer \
     '-Dmicronaut.router.static-resources.thermometer.mapping=/thermometer/**' \
     -jar target/EventPost-1.0-SNAPSHOT.jar
```

Then start TemperatureSender and open
<http://localhost:8080/thermometer/index.html>.

Opening `index.html` directly from disk doesn't work: the browser sends
`Origin: null` from a `file://` page, which EventPost refuses. To serve the
page from another web server instead, start EventPost with CORS enabled,
as described in the EventPost README. `run-chrome` is an older Linux script
that works around this by turning off Chrome's web security; serving the
page is the better choice.

## Settings

The server address, `http://localhost:8080`, is set in the `EventSource` URL
in `index.html`.
