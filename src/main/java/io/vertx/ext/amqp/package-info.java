/**
 * = Vert.x AMQP Service
 * :toc: top
 *
 * Vert.x AMQP Service allows AMQP 1.0 applications and Vert.x applications to communicate with each other by acting as a router+bridge which,
 *
 * * Routes between the AMQP and Vert.x address spaces.
 * * Translates between the message formats.
 *
 * It facilitates reliable communication via message passing, while maintaining QoS by allowing control of the message flow in both directions.
 *
 * _Supported interaction patterns include *request-reply* and *publish-subscribe*._
 *
 * image::http://people.apache.org/~rajith/vert.x/vertx-amqp.jpeg[]
 *
 * == Key Features:
 *
 * * Expose Vert.x Services to AMQP client applications.
 * * Expose AMQP Services to Vert.x client applications.
 * * Static and dynamic configuration of routes between the two address spaces.
 * * Allow different levels of reliability [unreliable (default) | at-least-once].
 * * Facilitate flow control (in both directions) to maintain Quality of Service.
 * * Management & Statistics(via AMQP 1.0 management protocol & hawt.io pluging - future release).
 *
 * == Prerequisites.
 * This documentation assumes that you are familiar with Vert.x, basic messaging concepts & AMQP 1.0 concepts.
 * The AMQP examples require Apache QPid Proton installed.
 *
 * * Vert.x https://vertx.ci.cloudbees.com/view/vert.x-3/job/vert.x3-website/ws/target/site/docs.html:[manual]
 * * AMQP https://amqp.org[AMQP Specification Group]
 * * Apache QPid Proton http://qpid.apache.org/proton[Install] | http://qpid.apache.org/proton[Python Manual]
 *
 *
 * == Deploying AMQP Service.
 * The first step is to deploy the AMQP Service (bridge) as a standalone service or programmatically.
 *
 * === Deploy it as a standalone service
 * Assuming the Vert.x AMQP Service jar and it's dependencies are located within the _lib directory_ of the vert.x installation
 * [source]
 * ----
 * vertx run service:io.vertx.vertx-amqp-service -cluster 
 * ----
 * [IMPORTANT]
 * ====
 * If you run it standalone, you need to make sure the +++<u>cluster</u>+++ option is used. Please refer to the main vert.x manual on how to configure clustering.
 * ====
 *
 * === Deploy it programmatically
 * [source,$lang]
 * ----
 * {@link examples.Examples#exampleDeployServiceVerticle}
 * ----
 *
 * == Running your first example
 * Lets start with a simple request-reply example involving a Vert.x app and an AMQP app.
 * We will first run the examples before going through the concepts and the code.
 *
 * === AMQP Client -> Vert.x Service
 * image::http://people.apache.org/~rajith/vert.x/vertx-service-amqp-client.jpeg[]
 *
 * * Step 1. Start the Vert.x-AMQP-Service. +
 * [source] 
 * ----
 * vertx run service:io.vertx.vertx-amqp-service -cluster 
 * ----
 * [IMPORTANT]
 * ====
 * Wait until you see the following message before you proceed with Step 2.
 * [source]
 * ----
 * AmqpService is now available via the address : vertx.service-amqp 
 * Succeeded in deploying verticle
 * ----
 * ====
 *
 * * Step 2. Run the HelloServiceVerticle as follows. +
 * The above class is located in _src/examples/request-reply/java_ folder within the source tree.
 * [source]
 * ----
 * vertx run HelloServiceVerticle.java -cluster
 * ----
 * [IMPORTANT]
 * ====
 * Wait until you see the following message before you proceed with Step 3.
 * [source]
 * ----
 * Succeeded in deploying verticle
 * ----
 * ====
 *
 * * Step 3. Run the AMQP Client as follows.
 * [IMPORTANT]
 * ====
 * * You need Apache QPid Proton installed and the PYTHON_PATH set properly before executing the AMQP examples.
 * See <<running_amqp_examples>> for more information.
 * * The scripts are located under src/amqp-examples.
 * * Use -h or --help to get list of all options.
 * ====
 * [source]
 * ----
 * ./client.py
 * ----
 *
 * If you plan to use a 3rd party intermediary for setting up the reply-to destination.
 * [source]
 * ----
 * ./client.py --response_addr <ip>:<port>/<dest-name>
 * ----
 *
 * ==== How it all works
 * * If you take a closer look at the AMQP client and the Vert.x Service you would see that it is no different from an ordinary AMQP app or Vert.x app.
 * __i.e no extra code is required on either side for basic communication__
 *
 * * The AMQP Client creates a request message with a reply-to address set and sends to the Vert.x-AMQP-Service.
 * [source,python]
 * ----
 * self.sender = event.container.create_sender(self.service_addr)
 * ...
 * event.sender.send(Message(reply_to=self.reply_to, body=request));
 * ----
 * * The Vert.x-AMQP-Service then translates the message into the json format and puts it into the Vert.x event-bus
 * * By default the AMQP Target is used as the event-bus address. You could configure a different mapping. See <<configuration>> for more details.
 * * The Vert.x Service (HelloServiceVerticle) listens on this address and receives this message.
 * [source, java]
 * ----
 * vertx.eventBus().consumer("hello-service-vertx", this);
 * ----
 * * Once received, it prepares the response (in this case appends hello to the request msg and uppercase the string) and replies on the message.
 * * The reply is received by the Vert.x-AMQP-Service which then forwards it to the AMQP client.
 *
 * === Vert.x Client -> AMQP Service
 * image::http://people.apache.org/~rajith/vert.x/amqp-service-vertx-client.jpeg[]
 *
 * * Step 1. Start the Vert.x-AMQP-Service. +
 * ** Start the Vert.x AMQP Service with the correct configuration. For this example some config is required.
 * ** The config required for this example is located in _src/examples/request-reply_ folder within the source tree.
 * [source] 
 * ----
 * vertx run service:io.vertx.vertx-amqp-service -conf ./request-reply.json -cluster 
 * ----
 * [IMPORTANT]
 * ====
 * Wait until you see the following message before you proceed with Step 2.
 * [source]
 * ----
 * AmqpService is now available via the address : vertx.service-amqp 
 * Succeeded in deploying verticle
 * ----
 * ====
 *
 * * Step 2. Run the AMQP Service as follows.
 * [IMPORTANT]
 * ====
 * * You need Apache QPid Proton installed and the PYTHON_PATH set properly before executing the AMQP examples.
 * See <<running_amqp_examples>> for more information.
 * * The scripts are located under src/amqp-examples.
 * * Use -h or --help to get list of all options.
 * ====
 * [source]
 * ----
 * ./hello-service.py
 * ---- 
 *
 * * Step 3. Run the ClientVerticle as follows. +
 * The above class is located in _src/examples/request-reply/java_ folder within the source tree.
 * [source]
 * ----
 * vertx run ClientVerticle.java -cluster 
 * ----
 * ==== How it all works
 * * If you take a closer look at the AMQP Service and the Vert.x Client you would see that it is no different from an ordinary AMQP app or Vert.x app.
 * __i.e no extra code is required on either side for basic communication__. A little bit of configuration is required though.
 *
 * * The Vert.x clients creates a request message and sends it to the Vert.x event-bus using 'hello-service-amqp' as the address. It also registers a reply-to handler.
 * [source,java]
 * ----
 * JsonObject requestMsg = new JsonObject();
 * requestMsg.put("body", "rajith muditha attapattu");
 * vertx.eventBus().send("hello-service-amqp", requestMsg, this);
 * ----
 * * The Vert.x-AMQP-Service is configured to listen on the Vert.x event-bus for any messages sent to 'hello-service-amqp' and then forward it to the correct AMQP endpoint. +
 *   The reply-to address in the AMQP message is set to point to the Vert.x-AMQP-Service and it keeps a mapping to the Vert.x reply-to.
 * [source, JSON]
 * ----
 * "vertx.handlers" : ["hello-service-amqp"]
 * "vertx.routing-outbound" : {
 "routes" :{
 "hello-service-amqp" : "amqp://localhost:5672/hello-service-amqp"
 }

 }
 * ----
 * * The AMQP Service receives the request, appends hello, upper case the string and sends it to reply-to address.
 * [source, python]
 * ----
 * sender = self.container.create_sender(event.message.reply_to)
 * greeting = 'HELLO ' + request.upper()
 * delivery = sender.send(Message(body=unicode(greeting)))
 * ----
 * * The Vert.x-AMQP-Service which receives the response, looks up the mapping and forwards it to the ClientVerticle via the event-bus.
 *
 * [[routing]]
 * == How routing works
 * The Vert.x-AMQP-Service acts as a router between the AMQP and Vert.x space.
 * This section provides insight into how the routing works and how it can be configured at deploy time and runtime.
 *
 * === Inbound Routing
 * When a message is received by the Vert.x-AMQP-Service from an AMQP peer
 *
 * * It checks to see if the Vert.x-AMQP-Service knows about the __'incoming AMQP link'__ associated with the message.
 * * If it has an association to a _Vert.x address_, the message will be forwarded to this Vert.x address via the event bus. +
 *   These associations are created,
 *
 *   ** When a Vert.x application establishes incoming links via the Service API. See <<incoming-link>>
 *   ** When an AMQP application sends messages to a known __Vert.x 'Service'__ registered with the Vert.x-AMQP-Service via the Service API. See <<exposing-service>>
 *
 * * If there is no known association, it will use the chosen _message-property_ to lookup the routing table. (see <<configuration>>)
 * * If the value of that __message-property__ matches a Vert.x address, the message will be forwarded to that address via the event-bus.
 * * If there is no match the message will be sent on the event-bus using one of the following.
 *
 *  ** If a default-inbound-address (a.k.a dead-letter address) is specified (via 'vertx.default-inbound-address'), it will be sent to that address.
 *  ** If no default address is specified, it will use the __'target'__ field for the given link as the address.
 *
 * === Outbound Routing
 * When a message is received by the Vert.x-AMQP-Service from a Verticle via the event-bus
 *
 * * It checks if the Vert.x address the message was sent to, have a known association with an _'outgoing AMQP link'_.
 * * If such an association is found, the message will be dispatched via that AMQP link. +
 *   These associations are created,
 *
 *   ** When a Vert.x application establishes outgoing links via the Service API. See <<outgoing-link>>
 *
 * * If no such association is found, it looks for a message-property in the following order and use the value of it to look up the routing table.
 *
 *   1. If 'vertx.routing-key' is specified it will use the value of it as the lookup key (overrides everything below).
 *   2. If not specified & a custom property is specified via _**routing-property-type=CUSTOM**_ & _**routing-property-name=<property-name>**_.
 *      It will look for it in the following order,
 *
 *      . Look for that property as a top-level property within the json message.
 *      . Look for that property within the __'properties'__ section within the json message.
 *      . Look for that property within the __'application-properties'__ section within the json message.
 *
 *   3. If not specified it will simply use the Vert.x address the message was sent to as the lookup key.
 *
 * * If the routing table has no matching entry, it will send the message to the default outbound address (dead-letter queue) configured via __'amqp.default-outbound-address'__.  
 *
 * [[configuration]]
 * == Configuring Vert.x AMQP Service.
 * Static configuration is specified via a json file at deployment time. Please check the examples above for sample configuration files.
 * [NOTE]
 * ====
 * Please note all configuration is optional.
 * ====
 * [width="100%",cols="8,8,16",options="header"]
 * .Config Options
 * |===
 * |Option | Default | Description
 * |address| vertx.service-amqp| The address for sending messages (method calls) to the Vert.x AMQP Service
 * |amqp.inbound-host| localhost| Specifies the host ip for inbound AMQP connections.
 * |amqp.inbound-port| 5673| Specifies the port for inbound AMQP connections
 * |amqp.default-outbound-address| amqp://localhost:5672/vertx | dead-letter-queue for unmatched outbound message.
 * |vertx.default-handler-address| vertx.service-amqp.bridge| The default address for sending messages (content) to the Vert.x AMQP Service to be routed into the AMQP space.
 * |vertx.handlers| []| A list of additional Vert.x event-bus addresses the AMQP Service should listen on.
 * |vertx.default-inbound-address | NULL | | dead-letter-address for unmatched inbound message.
 * |vertx.routing-outbound| {}| A map configuring outbound routing, including routes.
 * See 'Table 2. vertx.routing-outbound'.
 * |vertx.routing-inbound| {}| A map configuring inbound routing, including routes.
 * See 'Table 3. vertx.routing-inbound'.
 * |===
 *
 *
 * [width="100%",cols="8,8,16",options="header"]
 * .vertx.routing-outbound
 * |===
 * |Option | Default | Description
 * |routing-property-name| Vert.x event-bus address| If specified the router will look for that property within the outbound JSON message in the following order.
 *
 * 1. As a top-level property. +
 * 2. If a __'properties'__ map is specified, within that map. +
 * 3. If an __'application_properties'__ map is specified, within that map.
 *
 * |routes| {}| A map containing entries that map a 'routing-key' (as extracted above) to an AMQP endpoint address.
 * See # <1>  
 * |===
 *
 * [source]
 * .<1> Outbound routes example.
 * ----
 * "routes" :{
 *              "hello-service-amqp" : "amqp://localhost:5672/hello-service-amqp"
 *              "fortune-cookie-service" :  "amqp://localhost:7772/fortune-cookie-service"
 *            }
 * ----
 *
 *
 * [width="100%",cols="8,8,16",options="header"]
 * .vertx.routing-inbound
 * |===
 * |Option | Default | Description
 * |routing-property-type| ADDRESS| One of [ADDRESS, SUBJECT, CUSTOM].
 *
 * If CUSTOM is selected, then you need to specify _'routing-property-name'_
 * |routing-property-name| mandatory | Looks for this property within the Application Properties in an AMQP message.
 *
 * |routes| {}| A map containing entries that map a 'routing-key' (as extracted above) to an a Vert.x address.
 * See # <2>  
 * |===
 *
 * [source]
 * .<2> Inbound routes example.
 * ----
 * "routes" :{
 *             "amqp://localhost:5673/foo.*" : "foo-all",
 *             "amqp://localhost:5673/foo.bar*" : "foo-bar"
 *           }
 * ----
 *
 * == AmqpService Interface
 * The AmqpService interface allows a Vert.x application to interact with the Vert.x-AMQP-Service (bridge) and leverage some of the important features of AMQP.
 * Please refer to the API documentation for more information.
 *
 * === AmqpService access via Proxy.
 * [source, $lang]
 * ----
 * {@link examples.Examples#obtainingRefToServiceProxy}
 * ----
 *
 * === Link Management
 *
 * [[outgoing-link]]
 * ==== Setting up & destroying an outgoing link.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#establishOutgoingLink}
 * ----
 * <1> The AMQP Endpoint address to which you want to send messages.
 * <2> The event-bus address which would be mapped to the above link. The Verticle would be sending messages to this event-bus address.
 * <3> The event-bus address to which notifications about the incoming link is sent.  Ex. Errors, Delivery Status, credit
 * availability. The application should register a handler with the event-bus to receive these updates.
 * <4> Uses the options object to specify the desired level of reliability. Default is UNRELIABLE.
 * <5> The AsyncResult contains a ref (string) to the mapping created. This is required when changing behavior or canceling the link and it' association.
 * <6> The outgoing link is closed and the mapping btw it and the event-bus address is removed.
 *
 * [[incoming-link]]
 * ==== Setting up & destroying an incoming link.
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#establishIncomingLink}
 * ----
 * <1> The AMQP Endpoint address from which you want to receive messages (subscription).
 * <2> The event-bus address which would be mapped to the above link. The Verticle would be reiving messages via this event-bus address.
 * <3> The event-bus address to which notifications about the incoming link is sent. Ex. Errors. The application should register a handler with the event-bus to receive these updates.
 * <4> Uses the options object to specify the desired level of reliability. Default is UNRELIABLE.
 * <5> The amount of messages to prefetch. __Defaults to "1". __ +
 *     __If set to a value > 0__, the Vert.x-AMQP-Service will automatically fetch more messages when a certain number of messages are marked as
 * either accepted, rejected or released. The Vert.x-AMQP-Service will determine the optimum threshold for when the fetch happens and how much
 * to fetch. +
 *    __If set to "0"__, the vert.x application will need to explicitly request messages using AmqpService#fetch(String, int, io.vertx.core.Handler).
 * <6> The AsyncResult contains a ref (string) to the mapping created. This is required when changing behavior or canceling the link and it' association.
 * <7> The incoming link is closed and the mapping btw it and the event-bus address is removed.  
 *
 * === Sending Messages 
 *
 * ==== Sending a message reliably.
 * Messages are sent asynchronously and delivery confirmations are sent to the notification address.
 * [source, $lang]
 * ----
 * {@link examples.Examples#sendingMessagesReliably}
 * ----
 * <1> Set a unique reference. The application then uses this ref to correlate a delivery confirmation to a sent message.
 * <2> Sending the message via the event-bus.
 * <3> Subscribing to the event-bus to receive notifications.
 * <4> Use NotificationHelper class to parse the notification message.
 * <5> Retrieve the delivery state. Whether it's SETTLED, or in doubt (UNKNOWN, LINK_FAILURE) due to some error.
 * <6> Retrieve the message state. One of ACCEPTED, REJECTED or RELEASED.
 *
 * ==== Respecting flow control when sending.
 * This allows the receiving application (AMQP app) to be in control of many message it can receive at any given time.
 * [source, $lang]
 * ----
 * {@link examples.Examples#respectingFlowControlRequirements}
 * ----
 * <1> Subscribing to the event-bus to receive notifications.
 * <2> Use NotificationHelper class to parse the notification message.
 * <3> Use NotificationHelper.getCredits() method to retrieve the credits given by the receiving app.
 *
 * ==== Setting AMQP message properties when sending.
 * [source, $lang]
 * ----
 * {@link examples.Examples#settingAMQPMessageProperties}
 * ----
 * <1> Use "body" to set the message content.
 * <2> The message-translator will look for "properties" and inspect it to look for the items below that will be mapped to fields in AMQP Properties.
 * <3> The "subject" will be mapped AMQP Property subject.
 * <4> The "reply-to" will be mapped AMQP Property reply-to.
 * <5> The "message-id" will be mapped AMQP Property message-id.
 * <6> The "correlation-id" will be mapped AMQP Property correlation-id.
 * <7> The message-translator will look for "application-properties" and copy all the contents into the AMQP application-properties.
 * <8> Application defined Key-Value pairs, that will be copied into AMQP application-properties.
 *
 * === Receiving Messages
 *
 * ==== Fetching messages explicitly when prefetch is disabled.
 * [source, $lang]
 * ----
 * {@link examples.Examples#fetchingMessages}
 * ----
 * <1> The link reference obtain when setting up the link.
 * <2> The number of messages to fetch.
 *
 * ==== Receiving messages reliably.
 * [source, $lang]
 * ----
 * {@link examples.Examples#receivingMessagesReliably}
 * ----
 * <1> Accepting the message by passing the __'INCOMING_MSG_REF'__
 *     The Vert.x-AMQP-Service uses this ref to lookup the correct AMQP message and accepts it. +
 *     Simillary you could __reject__ & __release__ messages.
 *
 * ==== Retrieving AMQP message properties when receiving.
 * [source, $lang]
 * ----
 * {@link examples.Examples#retrievingAMQPMessageProperties}
 * ----
 * <1> Use "body" to get the message content.
 * <2> The message-translator will retieve fields in AMQP Properties to place it under "properties" section of the json message as stated below
 * <3> The AMQP Property subject will be mapped to "subject".
 * <4> The AMQP Property reply-to will be mapped to "reply-to".
 * <5> The AMQP Property message-id will be mapped to "message-id".
 * <6> The AMQP Property correlation-id will be mapped "correlation-id".
 * <7> The message-translator will copy any entries within AMQP application-properties into "application-properties" section of the json message.
 *
 * [[exposing-service]]
 * === Exposing a Vert.x Service via AMQP.
 * The first example we looked at exposed a Vert.x service by simply mapping an event-bus address to an AMQP endpoint.
 * The AMQP endpoint was managed by the Vert.x-AMQP-Service (bridge) and forwarded any requests to the Vert.x event-bus address.
 *
 * However the communication was unreliable and flow control was not within the explicit control of the Vert.x application.
 * The focus there was simplicity and no AMQP specifc interface or code was used.
 *
 * Lets now look at how a __service__ could register with the Vert.x-AMQP-Service to gain more control on how it want to interact with AMQP clients.
 *
 * ==== Registering a __Service__.
 * [source, $lang]
 * ----
 * {@link examples.Examples#registerService}
 * ----
 * <1> The event-bus address used when registering the service. 
 *     The service will be listening on this address via the event-bus for requests.
 * <2> Notification address to receive various notifications, including errors.
 * <3> Sets the initial capacity (no of requests allowed) for a new client wanting to use the service.
 *     __The default is '0'__, which means the service needs to explicity grant credits via the "issueCredits" methods (see below) for a client to be able send requests.
 * <4> De-registering the service from Vert.x-AMQP-Service.
 *
 * ==== Managing __clients__
 * This sections shows you how to,
 * * Identify a client uniquely
 * * How to control the flow of requests by managing request credits.
 * [source, $lang]
 * ----
 * {@link examples.Examples#manageClients}
 * ----
 * <1> Subscribing to the event-bus to receive notifications.
 * <2> Use NotificationHelper class to parse the notification message.
 * <3> Use NotificationHelper.getLinkRef() method to retrieve the link-ref that uniquely identifies the client.
 * <4> Use service.issueCredits(<link-ref>, <request-credits>) to allow the client to send a request(s).
 *     In this example, the Verticle issues an initial request credit when a new link (client) is opened.
 *     Subsequently you could use service.issueCredits(<link-ref>, <request-credits>) to __**issue further credits**__ any time the Verticle (Vert.x Service) deems necessary.
 *
 * === Managing Routes
 * The following examples show how the routing tables can be manipulated at runtime via the AmqpService interface.
 * For more info on how routing works, see <<routing>>
 *
 * [source, $lang]
 * ----
 * {@link examples.Examples#manageRoutes}
 * ----
 * <1> Adds an entry to to the incoming routing table. The pattern is applied to the extracted routing-key and if matched, will be fowarded to the given Vert.x event-bus addresses.
 * <2> Removes the entry from the incoming routing table.
 * <3> Adds an entry to to the outgoing routing table. The pattern is applied to the extracted routing-key and if matched, will be fowarded to the given AMQP addresses.
 * <4> Removes the entry from the outgoing routing table.
 *
 * == Putting it all together.
 * Lets look at an example that puts the above concepts into use.
 *
 * * We will look at how the Vert.x app FortuneCookie-Service is able to service several AMQP Clients in a reliable manner, while being in control of the message flow at all times.
 *   This prevents the service from being overwhelmed with requests.
 *
 * * Next we look at how Vert.x client apps could access the AMQP app FortuneCookie-Service in a reliable manner, while respecting the flow control requirements imposed by the AMQP Service.
 *
 * The diagram below describes the interaction pattern for both examples.
 *
 * image::http://people.apache.org/~rajith/vert.x/example1.jpeg[]
 *
 * === AMQP Client -> Vert.x Service
 *
 * * Step 1. Start the Vert.x-AMQP-Service. +
 * [source] 
 * ----
 * vertx run service:io.vertx.vertx-amqp-service -cluster 
 * ----
 * [IMPORTANT]
 * ====
 * Wait until you see the following message before you proceed with Step 2.
 * [source]
 * ----
 * AmqpService is now available via the address : vertx.service-amqp 
 * Succeeded in deploying verticle
 * ----
 * ====
 *
 * * Step 2. Run the FortuneCookieServiceVerticle as follows. +
 * The above class is located in _src/examples/fortunecookie/java_ folder within the source tree.
 * [source]
 * ----
 * vertx run FortuneCookieServiceVerticle.java -cluster
 * ----
 * [IMPORTANT]
 * ====
 * Wait until you see the following message before you proceed with Step 3.
 * [source]
 * ----
 * Succeeded in deploying verticle
 * ----
 * ====
 *
 * * Step 3. Run the AMQP Client as follows.
 * [IMPORTANT]
 * ====
 * * You need Apache QPid Proton installed and the PYTHON_PATH set properly before executing the AMQP examples.
 * See <<running_amqp_examples>> for more information.
 * * The scripts are located under src/amqp-examples.
 * * Use -h or --help to get list of all options.
 * ====
 * [source]
 * ----
 * ./fortune-cookie-client.py
 * ----
 *
 * You could start additional clients and observe that the Vert.x service is in control at all times without being overwhelmed by additional clients.
 *
 * === Vert.x Client -> AMQP Service
 *
 * * Step 1. Start the Vert.x-AMQP-Service. +
 * [source] 
 * ----
 * vertx run service:io.vertx.vertx-amqp-service -cluster 
 * ----
 * [IMPORTANT]
 * ====
 * Wait until you see the following message before you proceed with Step 2.
 * [source]
 * ----
 * AmqpService is now available via the address : vertx.service-amqp 
 * Succeeded in deploying verticle
 * ----
 * ====
 *
 * * Step 3. Run the AMQP FortuneCookie Service as follows.
 * [IMPORTANT]
 * ====
 * * You need Apache QPid Proton installed and the PYTHON_PATH set properly before executing the AMQP examples.
 * See <<running_amqp_examples>> for more information.
 * * The scripts are located under src/amqp-examples.
 * * Use -h or --help to get list of all options.
 * ====
 * [source]
 * ----
 * ./fortune-cookie-service.py
 * ----
 *
 * * Step 4. Run the FortuneCookieClientVerticle as follows. +
 * The above class is located in _src/examples/fortunecookie/java_ folder within the source tree.
 * [source]
 * ----
 * vertx run FortuneCookieClientVerticle.java -cluster
 * ----
 *
 * You could start additional clients (Verticles) and observe that the AMQP service is in control at all times without being overwhelmed by additional clients.
 *
 *
 * [[running_amqp_examples]]
 * == Running the AMQP examples.
 * The AMQP examples require Apache QPid Proton installed.
 *
 * * Setting up the env
 * For ease of use, the AMQP examples are written using the Proton Python API.
 * Use the links below to setup the environment.
 *
 * * Apache QPid Proton https://git-wip-us.apache.org/repos/asf?p=qpid-proton.git;a=blob_plain;f=INSTALL.md;hb=0.9.1[Install]
 * * http://qpid.apache.org/releases/qpid-proton-0.9.1/proton/python/tutorial/tutorial.html[Python Tutorial]
 *
 * * Using a 3rd party AMQP intermediary
 * The examples are using the Vert.x-AMQP-Service (bridge) as an intermediary when required.
 * Ex. for setting up a temp destination for replies. 
 * But you could use a 3rd part AMQP service just as well. (Ex. Message Broker or Router)
 *
 * ** Apache QPid Dispatch Router http://qpid.apache.org/components/dispatch-router[Manual]
 * ** Apache ActiveMQ http://activemq.apache.org[Website] | http://activemq.apache.org/amqp.html[AMQP config]
 *
 * @author <a href="mailto:rajith@rajith.lk">Rajith Muditha Attapattu</a>
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-amqp", groupPackage = "io.vertx") package io.vertx.ext.amqp;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;