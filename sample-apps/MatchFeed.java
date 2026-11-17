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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Match Center, Java subscriber: a live text feed of the match.
 *
 * The browser Scorekeeper (match-center.html) publishes the full match state
 * to the "match" topic. This program subscribes to the same topic and prints
 * each new event and featured shout-out as it happens, the way a stadium's
 * ticker display or a commentary feed might. Browsers and Java programs
 * receive exactly the same messages.
 *
 * Run:  java MatchFeed.java
 */
public class MatchFeed {

    static final String TOPIC = "match";

    // A string value in the match JSON, allowing for escaped characters
    static final String STRING = "\"((?:[^\"\\\\]|\\\\.)*)\"";
    static final Pattern TEAM = Pattern.compile("\"(home|away)\":\\{\"name\":" + STRING + "[^}]*\"score\":(\\d+)");
    static final Pattern EVENT = Pattern.compile("\"minute\":" + STRING + ",\"text\":" + STRING);
    static final Pattern SHOUTOUT = Pattern.compile("\"shoutout\":\\{\"name\":" + STRING + ",\"text\":" + STRING);

    static String lastEvent = "";
    static String lastShoutout = "";

    public static void main(String[] args) throws Exception {
        EventPostClient client = new EventPostClient();
        System.out.println("Following the '" + TOPIC + "' topic...");

        // The state arrives every second or so; print only what's new
        client.subscribe(EventPostClient.Type.TOPIC, TOPIC, state -> {
            String event = lastMatch(EVENT, state);
            if (!event.isEmpty() && !event.equals(lastEvent)) {
                lastEvent = event;
                System.out.println(event + "    " + score(state));
            }

            String shoutout = lastMatch(SHOUTOUT, state);
            if (!shoutout.equals(lastShoutout)) {
                lastShoutout = shoutout;
                if (!shoutout.isEmpty()) {
                    System.out.println("    📣 " + shoutout);
                }
            }
        });
    }

    // "Lions 2 - 1 Falcons"
    static String score(String state) {
        Matcher m = TEAM.matcher(state);
        String home = "", away = "";
        while (m.find()) {
            String team = unescape(m.group(2)) + " " + m.group(3);
            if (m.group(1).equals("home")) home = team; else away = team;
        }
        return home + " - " + away;
    }

    // The last match of a two-part pattern, as "first second", or ""
    static String lastMatch(Pattern pattern, String state) {
        Matcher m = pattern.matcher(state);
        String result = "";
        while (m.find()) {
            result = unescape(m.group(1)) + (pattern == SHOUTOUT ? ": " : "  ") + unescape(m.group(2));
        }
        return result;
    }

    static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
