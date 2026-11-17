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

package sselibrary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author ericjbruno
 */
public class SSEDataSubscriber extends Thread {
    static boolean debug = false;

	String serverURL = null;
	String authCode = null;
	String destination = null;
	SSECallback callback = null;
	
	public enum DestinationType {
		QUEUE, TOPIC
	}
	
	public SSEDataSubscriber(	String serverURI,
								DestinationType type,
								String authCode ) {
		this.authCode = authCode;
		if ( type == DestinationType.QUEUE ) {
			this.serverURL = serverURI + "/api/queue/";
		}
		else {
			this.serverURL = serverURI + "/api/topic/";
		}
	}
	
	public void subscribe( String destination, SSECallback callback ) {
		// Make sure thread hasn't started yet (you can only subscribe once)
		// and all the information is provided
		//
		System.out.println("Subscribing to " + serverURL + destination );
		if ( ! this.isAlive() && destination != null && callback != null ) {
			this.destination = destination;
			this.callback = callback;
			this.serverURL += destination;
			this.start();
		}
	}
	
	public void run() {
		try {
			// Send the request
			//
			URL url = new URL(serverURL);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setConnectTimeout(0);

			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;

			while ((line = rd.readLine()) != null) {
				// Make sure we received something of substance
				//
				if ( line != null && line.length() > 0 ) {
					// Did we get a heartbeat or useful data?
					//
					if ( line.startsWith(":") ) {
						if ( debug == true ) {
							System.out.println("heartbeat");
						}
					}
					else if ( line.startsWith("data:") ) {
						if ( debug == true ) {
							System.out.println(line.substring("data:".length()));
						}
						
						if ( callback != null ) {
							callback.onMessage(destination, line);
						}
					}
				}
			}

			rd.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
