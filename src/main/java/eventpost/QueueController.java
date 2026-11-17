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

package eventpost;

import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.sse.Event;
import io.micronaut.session.Session;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.netty.channel.Channel;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import eventpost.persist.MessagePersistance;
import eventpost.persist.MessagePersistanceFactory;
import eventpost.sse.Message;
import eventpost.sse.Messenger;
import eventpost.sse.Queue;
import org.reactivestreams.Publisher;

/**
 * @author ericjbruno
 */
// Served at /eventpost/api, and at /messageserver/api, the path used before
// the rename to EventPost, so existing clients keep working
@Controller
public class QueueController extends Messenger {

    private MessagePersistance messageDB
            = MessagePersistanceFactory.getInstance().getMessagePersistance();

    public QueueController() {
        log("QueueController");
        loadSavedMessages();
    }

    //
    // Queue Listener calls this. Returns a reactive streams
    // publisher object to emit SSE events
    //
    @Get(uris = {"/eventpost/api/queue/{name}", "/messageserver/api/queue/{name}"})
    @Produces(MediaType.TEXT_EVENT_STREAM)
    public Publisher<Event<String>> index(Optional<String> name, HttpRequest<?> request) {
        // Determine queue to listen to
        //
        System.out.println("Queue name: " + name);
        Queue dest = getQueue(name.get());

        // The receiver's connection. Micronaut only cancels the stream after
        // a write to a closed connection fails, which is too late: the message
        // written would be lost. Netty marks the channel inactive when it
        // reads the close, but Micronaut turns off automatic reading while it
        // sends a response, so ask for a read: a receiver sends nothing after
        // its request, so the next thing read is the close itself
        Channel channel = (request instanceof NettyHttpRequest<?> netty)
                ? netty.getChannelHandlerContext().channel()
                : null;
        watchForClose(channel);

        // How many more messages the connection has asked for. Messages are
        // taken from the queue only against this demand, so they aren't taken
        // faster than they can be sent
        AtomicLong demand = new AtomicLong();

        return Flowable.<Event<String>>create(emitter -> {
            Message msg = null;
            try {
                while (!emitter.isCancelled() && connected(channel)) {
                    // Wait until the connection can accept another message
                    if (demand.get() == 0) {
                        synchronized (demand) {
                            if (demand.get() == 0) {
                                demand.wait(1000);
                            }
                        }
                        continue;
                    }

                    // Wait a second at a time, so a receiver that has gone is
                    // noticed instead of being handed the next message
                    msg = dest.pollNextMessage(1, TimeUnit.SECONDS);
                    if (msg == null) {
                        watchForClose(channel);
                        continue;
                    }

                    if (emitter.isCancelled() || !connected(channel)) {
                        // The receiver went away while we waited; keep the message
                        break;
                    }

                    // Deliver it, then delete it
                    emitter.onNext(Event.of(new String(msg.getData())));
                    dest.deleteMessage(msg.getId());
                    msg = null;
                    demand.getAndUpdate(n -> n == Long.MAX_VALUE ? n : n - 1);
                }
            } catch (InterruptedException e) {
                // The stream was cancelled while waiting
            } finally {
                if (msg != null) {
                    dest.returnMessage(msg.getId());
                }
            }
            // The loop only ends when the receiver has gone, so there's no
            // one to complete the stream for
        }, BackpressureStrategy.BUFFER)
        .doOnRequest(n -> {
            demand.accumulateAndGet(n, (have, more) ->
                    have + more < 0 ? Long.MAX_VALUE : have + more);
            synchronized (demand) {
                demand.notifyAll();
            }
        })
        // requestOn=false: deliver requests on the caller's thread, since the
        // I/O thread is busy running the loop above
        .subscribeOn(Schedulers.io(), false);
    }

    //
    // Queue Sender calls this to send messages. They are persisted
    // and then delivered to at most one listener
    //
    @Consumes(MediaType.TEXT_EVENT_STREAM)
    @Post(uris = {"/eventpost/api/queue/publish", "/messageserver/api/queue/publish"})
    public HttpResponse<?> queue(Session session,
            HttpRequest<?> request,
            @Body String data) {
        try {
            HttpParameters params = request.getParameters();
            String queueName = params.getFirst("name").orElse(null);
            Queue dest = getQueue(queueName);

            return processSend(dest, data);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return HttpResponse.status(HttpStatus.UNAUTHORIZED, "Not authenticated");
    }

    private static boolean connected(Channel channel) {
        return channel == null || channel.isActive();
    }

    private static void watchForClose(Channel channel) {
        if (channel != null && channel.isActive()) {
            channel.read();
        }
    }

    private void loadSavedMessages() {
        ArrayList<String> queueNames = messageDB.getStoredDestinations();
        for (String queueName : queueNames) {
            // Get the queue (loads all queued messages as side effect)
            getQueue(queueName);
        }
    }

    public void log(String s) {
        System.out.println("[QueueController] " + s);
    }

}
