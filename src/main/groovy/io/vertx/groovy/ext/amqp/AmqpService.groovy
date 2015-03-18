/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.groovy.ext.amqp;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.ext.amqp.OutgoingLinkOptions
import io.vertx.groovy.core.Vertx
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.amqp.IncomingLinkOptions
/**
 * AMQP service allows a Vert.x application to,
 * <ul>
 * <li>Establish and cancel incoming/outgoing AMQP links, and map the link it to
 * an event-bus address.</li>
 * <li>Configure the link behavior</li>
 * <li>Control the flow of messages both incoming and outgoing to maintain QoS</li>
 * <li>Send and Receive messages from AMQP peers with different reliability
 * guarantees</li>
 * </ul>
 * 
 * For more information on AMQP visit www.amqp.org This service speaks AMQP 1.0
 * and use QPid Proton(http://qpid.apache.org/proton) for protocol support.
*/
@CompileStatic
public class AmqpService {
  final def io.vertx.ext.amqp.AmqpService delegate;
  public AmqpService(io.vertx.ext.amqp.AmqpService delegate) {
    this.delegate = delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static AmqpService createEventBusProxy(Vertx vertx, String address) {
    def ret= new io.vertx.groovy.ext.amqp.AmqpService(io.vertx.ext.amqp.AmqpService.createEventBusProxy((io.vertx.core.Vertx)vertx.getDelegate(), address));
    return ret;
  }
  /**
   * Allows an application to establish a link to an AMQP message-source for
   * receiving messages. The service will receive the messages on behalf of
   * the application and forward it to the event-bus address specified in the
   * consume method. The application will be listening on this address.
   * @param amqpAddress A link will be created to the the AMQP message-source identified by this address. .
   * @param eventbusAddress The event-bus address to be mapped to the above link. The application should register a handler for this address on the event bus to receive the messages.
   * @param notificationAddress The event-bus address to which notifications about the incoming link is sent. Ex. Errors. The application should register a handler with the event-bus to receive these updates.
   * @param options Options to configure the link behavior (Ex prefetch, reliability). {@link IncommingLinkOptions} (see <a href="../../../../../../../cheatsheet/IncomingLinkOptions.html">IncomingLinkOptions</a>)
   * @param result The AsyncResult contains a ref (string) to the mapping created. This is required when changing behavior or canceling the link and it' association.
   * @return A reference to the service.
   */
  public AmqpService establishIncommingLink(String amqpAddress, String eventbusAddress, String notificationAddress, Map<String, Object> options, Handler<AsyncResult<String>> result) {
    this.delegate.establishIncommingLink(amqpAddress, eventbusAddress, notificationAddress, options != null ? new io.vertx.ext.amqp.IncomingLinkOptions(new io.vertx.core.json.JsonObject(options)) : null, result);
    return this;
  }
  /**
   * If prefetch was set to zero, this method allows the application to
   * explicitly fetch a certain number of messages. If prefetch > 0, the AMQP
   * service will prefetch messages for you automatically.
   * @param incomingLinkRef The String ref return by the establishIncommingLink method. This uniquely identifies the incoming link and it's mapping to an event-bus address.
   * @param messages The number of message to fetch.
   * @param result Notifies if there is an error.
   * @return A reference to the service.
   */
  public AmqpService fetch(String incomingLinkRef, int messages, Handler<AsyncResult<Void>> result) {
    this.delegate.fetch(incomingLinkRef, messages, result);
    return this;
  }
  /**
   * Allows an application to cancel an incoming link and remove it's mapping
   * to an event-bus address.
   * @param incomingLinkRef The String ref return by the establishIncommingLink method. This uniquely identifies the incoming link and it's mapping to an event-bus address.
   * @param result Notifies if there is an error.
   * @return A reference to the service.
   */
  public AmqpService cancelIncommingLink(String incomingLinkRef, Handler<AsyncResult<Void>> result) {
    this.delegate.cancelIncommingLink(incomingLinkRef, result);
    return this;
  }
  /**
   * Allows an application to establish a link to an AMQP message-sink for
   * sending messages. The application will send the messages to the event-bus
   * address. The AMQP service will receive these messages via the event-bus
   * and forward it to the respective AMQP message sink.
   * @param amqpAddress A link will be created to the the AMQP message-sink identified by this address.
   * @param eventbusAddress The event-bus address to be mapped to the above link. The application should send the messages using this address.
   * @param notificationAddress The event-bus address to which notifications about the outgoing link is sent. Ex. Errors, Delivery Status, credit availability. The application should register a handler with the event-bus to receive these updates.
   * @param options Options to configure the link behavior (Ex reliability). {@link IncommingLinkOptions} (see <a href="../../../../../../../cheatsheet/OutgoingLinkOptions.html">OutgoingLinkOptions</a>)
   * @param result The AsyncResult contains a ref (string) to the mapping created. This is required when changing behavior or canceling the link and it' association.
   * @return A reference to the service.
   */
  public AmqpService establishOutgoingLink(String amqpAddress, String eventbusAddress, String notificationAddress, Map<String, Object> options, Handler<AsyncResult<String>> result) {
    this.delegate.establishOutgoingLink(amqpAddress, eventbusAddress, notificationAddress, options != null ? new io.vertx.ext.amqp.OutgoingLinkOptions(new io.vertx.core.json.JsonObject(options)) : null, result);
    return this;
  }
  /**
   * Allows an application to cancel an outgoing link and remove it's mapping
   * to an event-bus address.
   * @param outgoingLinkRef The String ref return by the establishOutgoingLink method. This uniquely identifies the outgoing link and it's mapping to an event-bus address.
   * @param result Notifies if there is an error.
   * @return A reference to the service.
   */
  public AmqpService cancelOutgoingLink(String outgoingLinkRef, Handler<AsyncResult<Void>> result) {
    this.delegate.cancelOutgoingLink(outgoingLinkRef, result);
    return this;
  }
  /**
   * Allows an application to accept a message it has received.
   * @param msgRef - The string ref. Use {@link AmqpMessage#getMsgRef()}
   * @param result Notifies if there is an error.
   * @return A reference to the service.
   */
  public AmqpService accept(String msgRef, Handler<AsyncResult<Void>> result) {
    this.delegate.accept(msgRef, result);
    return this;
  }
  /**
   * Allows an application to reject a message it has received.
   * @param msgRef - The string ref. Use {@link AmqpMessage#getMsgRef()}
   * @param result Notifies if there is an error.
   * @return A reference to the service.
   */
  public AmqpService reject(String msgRef, Handler<AsyncResult<Void>> result) {
    this.delegate.reject(msgRef, result);
    return this;
  }
  /**
   * Allows an application to release a message it has received.
   * @param msgRef - The string ref. Use {@link AmqpMessage#getMsgRef()}
   * @param result Notifies if there is an error.
   * @return A reference to the service.
   */
  public AmqpService release(String msgRef, Handler<AsyncResult<Void>> result) {
    this.delegate.release(msgRef, result);
    return this;
  }
  /**
   * Start the service
   */
  public void start() {
    this.delegate.start();
  }
  /**
   * Stop the service
   */
  public void stop() {
    this.delegate.stop();
  }
}
