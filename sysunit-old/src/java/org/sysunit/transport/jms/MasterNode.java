/*
 * Copyright (C) SpiritSoft, Inc. All rights reserved.
 *
 * This software is published under the terms of the SpiritSoft Software License
 * version 1.0, a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * $Id$
 */
package org.sysunit.transport.jms;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;

import org.apache.commons.messenger.Messenger;
import org.apache.commons.messenger.MessengerManager;
import org.sysunit.command.DispatchException;
import org.sysunit.command.Dispatcher;
import org.sysunit.command.master.MasterServer;
import org.sysunit.command.slave.RequestMembersCommand;
import org.sysunit.command.slave.StartTestNodeCommand;

/**
 * A Master Node implementation via JMS which establishes a network of nodes
 * during a specific period of time.
 * 
 * @author James Strachan
 * @version $Revision$
 */
public class MasterNode extends Node  {

	private MasterServer server = new MasterServer();
    private Dispatcher slaveGroupDispatcher;
    private long waitTime = 2000L;

    public static void main(String[] args) {
        // lets assume the messenger.xml is on the classpath
        String messengerName = "topicConnection";
		String groupSubject = "SYSUNIT.MASTERS";
		String slaveGroupSubject = "SYSUNIT.SLAVES";
        if (args.length > 0) {
            slaveGroupSubject = args[0];
        }

        try {
            Messenger messenger = MessengerManager.get(messengerName);
            if (messenger == null) {
                System.out.println("Could not find a messenger instance called: " + messengerName);
                return;
            }
			Destination groupDestination = messenger.getDestination(groupSubject);
			Destination slaveGroupDestination = messenger.getDestination(slaveGroupSubject);
            MasterNode controller = new MasterNode(new MasterServer(), messenger, groupDestination, slaveGroupDestination);
            controller.start();
        }
        catch (Exception e) {
            System.out.println("Caught: " + e);
            e.printStackTrace();
        }
    }

    public MasterNode(MasterServer server, Messenger messenger, Destination groupDestination, Destination slaveGroupDestination) throws JMSException {
    	super(server, messenger, groupDestination);
    	this.server = server;
    	
        slaveGroupDispatcher = new JmsDispatcher(messenger, slaveGroupDestination, getReplyToDestination());
    }

    public void start() throws Exception {

        // lets send an advertisement
        slaveGroupDispatcher.dispatch(new RequestMembersCommand());

        // now lets wait until some people arrive...
        Thread.sleep(waitTime);

        // now lets start kicking off the JVMs
        String xml = "fooSystemTest.jelly";
        String[] jvms = { "a", "b", "c" };

        roundRobbinJvms(xml, Arrays.asList(jvms));
    }

    // Properties
    //-------------------------------------------------------------------------    

    // Implementation methods
    //-------------------------------------------------------------------------    
    protected void roundRobbinJvms(String xml, List jvmNames) throws DispatchException {
        // lets create an Array of the dispatchers
        Collection dispatcherCollection = server.getMemberMap().values();
        int size = dispatcherCollection.size();
        Dispatcher[] dispatchers = new Dispatcher[size];
        dispatcherCollection.toArray(dispatchers);

        int idx = 0;
        for (Iterator iter = jvmNames.iterator(); iter.hasNext(); idx++) {
            String jvmName = (String) iter.next();

            if (idx >= size) {
                idx = 0;
            }
            dispatchers[idx].dispatch(new StartTestNodeCommand(xml, jvmName));
        }
    }
}
