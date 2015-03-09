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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;
import io.vertx.ext.amqp.MessagingException;
import io.vertx.ext.amqp.AmqpServiceConfig;

/**
 * Contains the routing logic used by the AMQP Service
 * 
 * @author <a href="mailto:rajith@redhat.com">Rajith Attapattu</a>
 *
 */
public class Router
{
    private static final Logger _logger = LoggerFactory.getLogger(Router.class);

    private final AmqpServiceConfig _config;

    public Router(AmqpServiceConfig config)
    {
        _config = config;

        if (_logger.isInfoEnabled())
        {
            StringBuilder b = new StringBuilder();
            b.append("Router Config \n[\n");
            b.append("Default vertx handler address : ").append(config.getDefaultHandlerAddress()).append("\n");
            b.append("Default vertx address : ").append(config.getDefaultInboundAddress()).append("\n");
            b.append("Default outbound address : ").append(config.getDefaultOutboundAddress()).append("\n");
            b.append("Handler address list : ").append(config.getHandlerAddressList()).append("\n");
            b.append("]\n");
            _logger.info(b.toString());
        }
    }

    /*
     * Looks at the Vertx address and provides the list of matching AMQP
     * addresses.
     */
    List<String> routeOutbound(String routingkey) throws MessagingException
    {
        List<String> addrList = new ArrayList<String>();
        for (String key : _config.getOutboundRoutes().keySet())
        {
            RouteEntry route = _config.getOutboundRoutes().get(key);
            if (route.getPattern().matcher(routingkey).matches())
            {
                addrList.addAll(route.getAddressList());
            }
        }
        return addrList;
    }

    /**
     * By default it uses the address field.
     * 
     * If a custom routing-property is specified in config it will look to see,
     * if that property is available <br>
     * 1. As field within the Json message <br>
     * 2. If a "properties" field is available, it will look for it as a sub
     * field under that <br>
     * 3. If an "application-properties" field is available, it will look for it
     * as a sub field under that
     * 
     * If no custome property is specified, then it looks if "vertx.routing-key"
     * is specified as a field within the Json message.
     */
    String extractOutboundRoutingKey(Message<JsonObject> m)
    {
        String routingKey = m.address(); // default
        if (_config.isUseCustomPropertyForOutbound() && _config.getOutboundRoutingPropertyName() != null)
        {
            if (m.body().containsKey(_config.getOutboundRoutingPropertyName()))
            {
                routingKey = m.body().getString(_config.getOutboundRoutingPropertyName());
            }
            else if (m.body().containsKey("properties") && m.body().getJsonObject("properties") instanceof Map
                    && m.body().getJsonObject("properties").containsKey(_config.getOutboundRoutingPropertyName()))
            {
                routingKey = m.body().getJsonObject("properties").getString(_config.getOutboundRoutingPropertyName());
            }
            else if (m.body().containsKey("application-properties")
                    && m.body().getJsonObject("application-properties") instanceof Map
                    && m.body().getJsonObject("application-properties")
                            .containsKey(_config.getOutboundRoutingPropertyName()))
            {
                routingKey = m.body().getJsonObject("application-properties")
                        .getString(_config.getOutboundRoutingPropertyName());
            }

            if (_logger.isInfoEnabled())
            {
                _logger.info("\n============= Custom Routing Property ============");
                _logger.info("Custom routing property name : " + _config.getOutboundRoutingPropertyName());
                _logger.info("Routing property value : " + routingKey);
                _logger.info("============= /Custom Routing Property ============/n");
            }
        }
        else if (m.body().containsKey("vertx.routing-key"))
        {
            routingKey = m.body().getString("vertx.routing-key");
        }
        return routingKey;
    }
    
    List<String> routeInbound(String routingKey)
    {
        List<String> addressList = new ArrayList<String>();
        if (_config.getInboundRoutes().size() == 0)
        {
            addressList.add(routingKey);
        }
        else
        {
            for (String k : _config.getInboundRoutes().keySet())
            {
                RouteEntry route = _config.getInboundRoutes().get(k);
                if (route.getPattern().matcher(routingKey).matches())
                {
                    addressList.addAll(route.getAddressList());
                }
            }
        }

        if (addressList.size() == 0 && _config.getDefaultInboundAddress() != null)
        {
            addressList.add(_config.getDefaultInboundAddress());
        }
        return addressList;
    }
    
    void addOutboundRoute(String address, String amqpURL)
    {
        if (_config.getOutboundRoutes().containsKey(address))
        {
            _config.getOutboundRoutes().get(address).add(amqpURL);
        }
        else
        {
            _config.getOutboundRoutes().put(address, AmqpServiceConfigImpl.createRouteEntry(_config, address, amqpURL));
        }
        if (_logger.isInfoEnabled())
        {
            _logger.info("\n============= Outbound Route ============");
            _logger.info(String.format("Adding a mapping for {%s : %s}", address, amqpURL));
            _logger.info("============= /Outbound Route) ============\n");
        }
    }
 
    void removeOutboundRoute(String address, String amqpURL)
    {
        if (_config.getOutboundRoutes().containsKey(address))
        {
            RouteEntry entry = _config.getOutboundRoutes().get(address);
            entry.remove(amqpURL);
            if (entry.getAddressList().size() == 0)
            {
                _config.getOutboundRoutes().remove(address);
            }
        }
    }
}