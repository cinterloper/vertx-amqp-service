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
package io.vertx.ext.amqp.impl;

import io.vertx.ext.amqp.ErrorCode;
import io.vertx.ext.amqp.MessagingException;
import io.vertx.ext.amqp.impl.protocol.DefaultConnectionSettings;

import java.util.ArrayList;
import java.util.List;

public class AddressParser {
  public static ConnectionSettings parse(String address) throws MessagingException {
    try {
      DefaultConnectionSettings settings = new DefaultConnectionSettings();
      int start = 0;
      int schemeEnd = address.indexOf("://", start);
      if (schemeEnd >= 0) {
        String scheme = address.substring(start, schemeEnd);
        settings.setScheme(scheme);
        start = schemeEnd + 3;
      }

      String uphp = address.substring(start);

      String hp;
      int at = uphp.indexOf('@');
      if (at >= 0) {
        String up = uphp.substring(0, at);
        hp = uphp.substring(at + 1);

        int colon = up.indexOf(':');
        if (colon >= 0) {
          String user = up.substring(0, colon);
          String pass = up.substring(colon + 1);
          settings.setUser(user);
          settings.setPass(pass);
        } else {
          String user = up;
          settings.setUser(user);
        }
      } else {
        hp = uphp;
      }

      int a = address.lastIndexOf('/');
      if (a > schemeEnd + 3) {
        String target = address.substring(a + 1);
        settings.setTarget(target);
        hp = hp.substring(0, hp.indexOf('/'));
      }

      String host = null;
      int port = 5672;
      if (hp.startsWith("[")) {
        int close = hp.indexOf(']');
        if (close >= 0) {
          host = hp.substring(1, close);
          if (hp.substring(close + 1).startsWith(":")) {
            port = Integer.parseInt(hp.substring(close + 2));
          }
        }
      }

      if (host == null) {
        int colon = hp.indexOf(':');
        if (colon >= 0) {
          host = hp.substring(0, colon);
          port = Integer.parseInt(hp.substring(colon + 1));
        } else {
          host = hp;
        }
      }
      settings.setHost(host);
      settings.setPort(port);

      return settings;
    } catch (Exception e) {
      throw new MessagingException("Error parsing address : " + e.getMessage(), e, ErrorCode.INVALID_ADDRESS_FORMAT);
    }
  }

  public static void main(String[] args) throws Exception {
    List<String> urls = new ArrayList<String>();
    urls.add("my-address");
    urls.add("example.org:1234");
    urls.add("amqps://fred:trustno1@example.org");
    urls.add("amqp://example.org:1234/outgoing");

    for (String url : urls) {
      ConnectionSettings settings = AddressParser.parse(url);
      StringBuilder b = new StringBuilder();
      b.append(url).append(" [host=").append(settings.getHost()).append("] [port=").append(settings.getPort())
        .append("] ");
      b.append("[target=").append(settings.getNode()).append("]");
      System.out.println(b.toString());
    }
  }
}