/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.ext.amqp;

/**
 * Outgoing message disposition
 */
public enum MessageState {
  /**
   * Message has been accepted by the remote peer.
   */
  ACCEPTED,

  /**
   * Message has been rejected by the remote peer.
   */
  REJECTED,

  /**
   * Message has been released by the remote peer.
   */
  RELEASED,

  /**
   * Disposition is not known.
   */
  UNKNOWN;
}