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

import io.vertx.ext.amqp.ConnectionSettings;
import io.vertx.ext.amqp.CreditMode;
import io.vertx.ext.amqp.MessagingException;
import io.vertx.ext.amqp.ReliabilityMode;

import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.Message;

/**
 * A Connection coupled with a session to simplify the RouterImpl.
 * 
 */
class ManagedConnection extends ConnectionImpl
{
    private final org.apache.qpid.proton.engine.Session _protonSession;

    private SessionImpl _session;

    private AmqpEventListener eventListener;

    ManagedConnection(ConnectionSettings settings, AmqpEventListener handler, boolean inbound)
    {
        super(settings, null, inbound);
        eventListener = handler;
        _protonSession = protonConnection.session();
        _session = new SessionImpl(this, _protonSession);
    }

    @Override
    public void open()
    {
        protonConnection.open();
        _protonSession.open();
        write();
    }

    @Override
    protected void processEvents()
    {
        protonConnection.collect(_collector);
        Event event = _collector.peek();
        while (event != null)
        {
            switch (event.getType())
            {
            case CONNECTION_REMOTE_OPEN:
                eventListener.onConnectionOpen(this);
                break;
            case CONNECTION_FINAL:
                eventListener.onConnectionClosed(this);
                break;
            case SESSION_REMOTE_OPEN:
                SessionImpl ssn;
                org.apache.qpid.proton.engine.Session amqpSsn = event.getSession();
                if (amqpSsn.getContext() != null)
                {
                    ssn = (SessionImpl) amqpSsn.getContext();
                }
                else
                {
                    ssn = new SessionImpl(this, amqpSsn);
                    amqpSsn.setContext(ssn);
                    event.getSession().open();
                }
                eventListener.onSessionOpen(ssn);
                break;
            case SESSION_FINAL:
                ssn = (SessionImpl) event.getSession().getContext();
                eventListener.onSessionClosed(ssn);
                break;
            case LINK_REMOTE_OPEN:
                Link link = event.getLink();
                if (link instanceof Receiver)
                {
                    InboundLinkImpl inboundLink;
                    if (link.getContext() != null)
                    {
                        inboundLink = (InboundLinkImpl) link.getContext();
                    }
                    else
                    {
                        inboundLink = new InboundLinkImpl(_session, link.getRemoteTarget().getAddress(), link,
                                ReliabilityMode.AT_LEAST_ONCE, CreditMode.AUTO);
                        link.setContext(inboundLink);
                        inboundLink.init();
                    }
                    eventListener.onInboundLinkOpen(inboundLink);
                }
                else
                {
                    OutboundLinkImpl outboundLink;
                    if (link.getContext() != null)
                    {
                        outboundLink = (OutboundLinkImpl) link.getContext();
                    }
                    else
                    {
                        outboundLink = new OutboundLinkImpl(_session, link.getRemoteSource().getAddress(), link);
                        link.setContext(outboundLink);
                        outboundLink.init();
                    }
                    eventListener.onOutboundLinkOpen(outboundLink);
                }
                break;
            case LINK_FLOW:
                link = event.getLink();
                if (link instanceof Sender)
                {
                    OutboundLinkImpl outboundLink = (OutboundLinkImpl) link.getContext();
                    eventListener.onOutboundLinkCredit(outboundLink, link.getCredit());
                }
                break;
            case LINK_FINAL:
                link = event.getLink();
                if (link instanceof Receiver)
                {
                    InboundLinkImpl inboundLink = (InboundLinkImpl) link.getContext();
                    eventListener.onInboundLinkClosed(inboundLink);
                }
                else
                {
                    OutboundLinkImpl outboundLink = (OutboundLinkImpl) link.getContext();
                    eventListener.onOutboundLinkClosed(outboundLink);
                }
                break;
            case TRANSPORT:
                // TODO
                break;
            case DELIVERY:
                onDelivery(event.getDelivery());
                break;
            default:
                break;
            }
            _collector.pop();
            event = _collector.peek();
        }
    }

    @Override
    void onDelivery(Delivery d)
    {
        Link link = d.getLink();
        if (link instanceof Receiver)
        {
            if (d.isPartial())
            {
                return;
            }

            Receiver receiver = (Receiver) link;
            byte[] bytes = new byte[d.pending()];
            int read = receiver.recv(bytes, 0, bytes.length);
            Message pMsg = Proton.message();
            pMsg.decode(bytes, 0, read);
            receiver.advance();

            InboundLinkImpl inLink = (InboundLinkImpl) link.getContext();
            SessionImpl ssn = inLink.getSession();
            InboundMessage msg = new InboundMessage(ssn.getID(), d.getTag(), ssn.getNextIncommingSequence(),
                    d.isSettled(), pMsg);
            eventListener.onMessage(inLink, msg);
        }
        else
        {
            if (d.remotelySettled())
            {
                TrackerImpl tracker = (TrackerImpl) d.getContext();
                tracker.setDisposition(d.getRemoteState());
                tracker.markSettled();
                eventListener.onSettled(tracker);
            }
        }
    }

    public OutboundLinkImpl createOutboundLink(String address, ReliabilityMode mode) throws MessagingException
    {
        OutboundLinkImpl link = (OutboundLinkImpl) _session.createOutboundLink(address, mode);
        link.init();
        write();
        return link;
    }

    public InboundLinkImpl createInboundLink(String address, ReliabilityMode receiverMode, CreditMode creditMode)
            throws MessagingException
    {
        InboundLinkImpl link = (InboundLinkImpl) _session.createInboundLink(address, receiverMode, creditMode);
        link.init();
        write();
        return link;
    }
}