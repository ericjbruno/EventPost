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

package test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Random;
import sselibrary.SSEDataPublisher;

/**
 * @author ericjbruno
 */
public class TemperatureSender {
	protected static String eventPostURI = "http://localhost:8080/eventpost";
	protected static String topicName = "temp1";
	protected static SSEDataPublisher dataPublisher = null;
	
    String cloudAuthString = "test:welcome1";
    
	protected static String authCode = null;

	Thread CANDataListener = null;
	boolean listening = true;
	
	//////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		// Read in CAN sender address and port, and the SSE server address and port
		//
		TemperatureSender.loadConfigs();
		
		try {
			TemperatureSender sender = new TemperatureSender();
		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
	}
	
	public TemperatureSender() throws Exception {
		dataPublisher = new SSEDataPublisher();
		log("SSE Data Publisher Started"); 

		// Generate fake temperature data
		//
		new Thread() {
			public void run() {
				int temp = 100;
				while ( true ) {
					try {
                        int delta = new Random().nextInt(10);
                        if ( delta < 5 ) {
                            temp -= 1;
                        }
                        else {
                            temp += 1;
                        }
                        
                        temp = Math.max(temp, 50);
                        temp = Math.min(temp, 150);
                        
						TemperatureSender.sendToServer(""+temp);
						sleep(250);
					}
					catch ( Exception e ) { }
				}
			}
		}.start();
	}
	
	protected static void loadConfigs() {
		// Attempt to load a properties file if found
		//
		Properties props = new Properties();

		try ( FileInputStream in = 
				new FileInputStream(
						new File("./sender.properties")) ) {

			props.load(in);

			TemperatureSender.eventPostURI = 
					props.getProperty("EventPostURI",
										props.getProperty("MessageServerURI",   // the name before EventPost
										TemperatureSender.eventPostURI));

		}
		catch ( Exception e ) {
			e.printStackTrace();
		}
		
		log("-----------------------------");
		log("Using EventPostURI="+eventPostURI);
		log("-----------------------------");
	}
	
	public static void sendToServer(String data) throws Exception {
		String url = eventPostURI + "/api/topic/publish?name=" + topicName;

		if ( dataPublisher != null ) {
			dataPublisher.sendMessage(url, data, authCode);
		}
	}
	
    public static void log(String s) {
        System.out.println("[TopicSender] " + s);
    }
}
