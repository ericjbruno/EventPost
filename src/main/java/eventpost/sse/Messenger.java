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

package eventpost.sse;

import io.micronaut.http.HttpResponse;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ericjbruno
 */
public class Messenger {

    protected final ConcurrentHashMap<String, Destination> destinations
            = new ConcurrentHashMap<>();

    protected boolean sendUpdate(String data, Vector<Object> listeners) {
        if (listeners != null) {
        }

        return false;
    }

    protected boolean sendSSEMessage(PrintWriter out, String data) {
        out.write("event: message\n");
        out.write("data: " + data + "\n\n");
        //out.flush();
        boolean error = out.checkError(); // no need to call flush()
        return !error;
    }

    public Queue getQueue(String name) {
        synchronized (destinations) {
            Queue dest = (Queue) destinations.get(name);
            if (dest == null) {
                dest = new Queue(name);
                destinations.put(name, dest);
            }
            return dest;
        }
    }

    public Topic getTopic(String name) {
        synchronized (destinations) {
            Topic dest = (Topic) destinations.get(name);
            if (dest == null) {
                dest = new Topic(name);
                destinations.put(name, dest);
            }
            return dest;
        }
    }

    public HttpResponse<String> processSend(Destination dest, String data) {
        // boolean event = false;
        // String id = "";

        try {
            String[] lines = data.split(System.getProperty("line.separator"));
            for (String line : lines) {
                // if (line.contains("event:")) {
                //     event = true;
                // } else if (line.contains("id:")) {
                //     id = line;
                // }
                if (line.contains("data:")) {
                    int start = line.indexOf("data:") + "data:".length();
                    data = line.substring(start).trim();
                    if (!dest.addMessage(data)) {
                        return HttpResponse.serverError("Failed to store message for " + dest.getName());
                    }
                }
            }
            return HttpResponse.ok(dest.getName()).setAttribute("Access-Control-Allow-Origin", "*");
        } 
        catch (Exception e) {
            return HttpResponse.serverError(e.toString());
        }

    }

    protected void removeListener(Object ac) {
        try {
            synchronized (destinations) {
                Iterator<Destination> iter = destinations.values().iterator();
                while (iter.hasNext()) {
                    Destination dest = iter.next();
                    if (dest.listeners.contains(ac)) {
                        dest.listeners.removeElement(ac);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
