/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package io.vertx.ext.amqp;

import io.vertx.ext.amqp.AMQPService;
import io.vertx.core.Vertx;
import io.vertx.core.Handler;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.serviceproxy.ProxyHandler;
import io.vertx.ext.amqp.ServiceOptions;
import io.vertx.ext.amqp.AMQPService;
import io.vertx.ext.amqp.OutgoingLinkOptions;
import io.vertx.core.Vertx;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.amqp.IncomingLinkOptions;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
public class AMQPServiceVertxProxyHandler extends ProxyHandler {

  public static final long DEFAULT_CONNECTION_TIMEOUT = 5 * 60; // 5 minutes 

  private final Vertx vertx;
  private final AMQPService service;
  private final long timerID;
  private long lastAccessed;
  private final long timeoutSeconds;

  public AMQPServiceVertxProxyHandler(Vertx vertx, AMQPService service) {
    this(vertx, service, DEFAULT_CONNECTION_TIMEOUT);
  }

  public AMQPServiceVertxProxyHandler(Vertx vertx, AMQPService service, long timeoutInSecond) {
    this(vertx, service, true, timeoutInSecond);
  }

  public AMQPServiceVertxProxyHandler(Vertx vertx, AMQPService service, boolean topLevel, long timeoutSeconds) {
    this.vertx = vertx;
    this.service = service;
    this.timeoutSeconds = timeoutSeconds;
    if (timeoutSeconds != -1 && !topLevel) {
      long period = timeoutSeconds * 1000 / 2;
      if (period > 10000) {
        period = 10000;
      }
      this.timerID = vertx.setPeriodic(period, this::checkTimedOut);
    } else {
      this.timerID = -1;
    }
    accessed();
  }

  public MessageConsumer<JsonObject> registerHandler(String address) {
    MessageConsumer<JsonObject> consumer = vertx.eventBus().<JsonObject>consumer(address).handler(this);
    this.setConsumer(consumer);
    return consumer;
  }

  private void checkTimedOut(long id) {
    long now = System.nanoTime();
    if (now - lastAccessed > timeoutSeconds * 1000000000) {
      close();
    }
  }

  @Override
  public void close() {
    if (timerID != -1) {
      vertx.cancelTimer(timerID);
    }
    super.close();
  }

  private void accessed() {
    this.lastAccessed = System.nanoTime();
  }

  public void handle(Message<JsonObject> msg) {
    try {
      JsonObject json = msg.body();
      String action = msg.headers().get("action");
      if (action == null) {
        throw new IllegalStateException("action not specified");
      }
      accessed();
      switch (action) {

        case "establishIncomingLink": {
          service.establishIncomingLink((java.lang.String)json.getValue("amqpAddress"), (java.lang.String)json.getValue("eventbusAddress"), (java.lang.String)json.getValue("notificationAddress"), json.getJsonObject("options") == null ? null : new io.vertx.ext.amqp.IncomingLinkOptions(json.getJsonObject("options")), createHandler(msg));
          break;
        }
        case "fetch": {
          service.fetch((java.lang.String)json.getValue("incomingLinkRef"), json.getValue("messages") == null ? null : (json.getLong("messages").intValue()), createHandler(msg));
          break;
        }
        case "cancelIncomingLink": {
          service.cancelIncomingLink((java.lang.String)json.getValue("incomingLinkRef"), createHandler(msg));
          break;
        }
        case "establishOutgoingLink": {
          service.establishOutgoingLink((java.lang.String)json.getValue("amqpAddress"), (java.lang.String)json.getValue("eventbusAddress"), (java.lang.String)json.getValue("notificationAddress"), json.getJsonObject("options") == null ? null : new io.vertx.ext.amqp.OutgoingLinkOptions(json.getJsonObject("options")), createHandler(msg));
          break;
        }
        case "cancelOutgoingLink": {
          service.cancelOutgoingLink((java.lang.String)json.getValue("outgoingLinkRef"), createHandler(msg));
          break;
        }
        case "accept": {
          service.accept((java.lang.String)json.getValue("msgRef"), createHandler(msg));
          break;
        }
        case "reject": {
          service.reject((java.lang.String)json.getValue("msgRef"), createHandler(msg));
          break;
        }
        case "release": {
          service.release((java.lang.String)json.getValue("msgRef"), createHandler(msg));
          break;
        }
        case "registerService": {
          service.registerService((java.lang.String)json.getValue("eventbusAddress"), (java.lang.String)json.getValue("notificationAddres"), json.getJsonObject("options") == null ? null : new io.vertx.ext.amqp.ServiceOptions(json.getJsonObject("options")), createHandler(msg));
          break;
        }
        case "unregisterService": {
          service.unregisterService((java.lang.String)json.getValue("eventbusAddress"), createHandler(msg));
          break;
        }
        case "issueCredits": {
          service.issueCredits((java.lang.String)json.getValue("linkId"), json.getValue("credits") == null ? null : (json.getLong("credits").intValue()), createHandler(msg));
          break;
        }
        case "addInboundRoute": {
          service.addInboundRoute((java.lang.String)json.getValue("pattern"), (java.lang.String)json.getValue("eventBusAddress"));
          break;
        }
        case "removeInboundRoute": {
          service.removeInboundRoute((java.lang.String)json.getValue("pattern"), (java.lang.String)json.getValue("eventBusAddress"));
          break;
        }
        case "addOutboundRoute": {
          service.addOutboundRoute((java.lang.String)json.getValue("pattern"), (java.lang.String)json.getValue("amqpAddress"));
          break;
        }
        case "removeOutboundRoute": {
          service.removeOutboundRoute((java.lang.String)json.getValue("pattern"), (java.lang.String)json.getValue("amqpAddress"));
          break;
        }
        case "start": {
          service.start();
          break;
        }
        case "stop": {
          service.stop();
          break;
        }
        default: {
          throw new IllegalStateException("Invalid action: " + action);
        }
      }
    } catch (Throwable t) {
      msg.fail(-1, t.getMessage());
      throw t;
    }
  }

  private <T> Handler<AsyncResult<T>> createHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        msg.reply(res.result());
      }
    };
  }

  private <T> Handler<AsyncResult<List<T>>> createListHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        msg.reply(new JsonArray(res.result()));
      }
    };
  }

  private <T> Handler<AsyncResult<Set<T>>> createSetHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        msg.reply(new JsonArray(new ArrayList<>(res.result())));
      }
    };
  }

  private Handler<AsyncResult<List<Character>>> createListCharHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  private Handler<AsyncResult<Set<Character>>> createSetCharHandler(Message msg) {
    return res -> {
      if (res.failed()) {
        msg.fail(-1, res.cause().getMessage());
      } else {
        JsonArray arr = new JsonArray();
        for (Character chr: res.result()) {
          arr.add((int) chr);
        }
        msg.reply(arr);
      }
    };
  }

  private <T> Map<String, T> convertMap(Map map) {
    return (Map<String, T>)map;
  }

  private <T> List<T> convertList(List list) {
    return (List<T>)list;
  }

  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>((List<T>)list);
  }
}