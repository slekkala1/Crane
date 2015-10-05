Tempest Architecture
====================

Overview
--------

A tempest is a like a storm but more windy and usually louder.  This is just what TheTribeHasSpoken Inc. needs!
We think you'll agree once you finish this brief architectural document.

Tempest consists of a socket **Gossip Server** and **Gossip Client** that use UDP sockets to Gossip, **Client** 
and **Server** for communication, a **Logger** which performs the ability to
write logs and to grep them, and there is a convenient **Console** built on Cliche(our only third party library
in the application module). The **TempestApp** is the entry point for the application and ties all of these classes
together in to a convenient application.

There are two modules in the git repository: an application module (tempest) and a test module (tempest-test).
The test module provides a suite of junit test to verify that the most important features of Tempest are
fully functional. The application module provides the fully functioning Tempest application.

Both modules use Maven for configuration management.  See the README.md file for more details about how to
get the application and test compiled and running using Maven.


Gossip protocol
---------------
An **Introduce** command is sent over TCP to the introducer when a member first starts the **MembershipService** and the member is added to the 
membership list of introducer. The introducer returns it's current membership list to the member and the member starts gossiping.
Gossiping of the membership list happens through **HeartBeat**, which increments the members heartbeat in the membership list and sends
the membership list using the **GossipClient** over UDP to a random member of the membership list other than itself every 250ms. 
A **GossipServer** runs on each member and recieves heartbeats. 

The **Leave** command is executed when a member stops it's membership, **Leave** is sent as over TCP to 
all known members so that a member can leave the group immedeatly. All members recieving a **Leave** message 
mark the leaving member as having left.  This prevents the leaving member from being further gossiped about.
The member who left will be removed from the membership list due to its heartbeat stopping the same as a crashed machine.

When a machine crashes, the hearbeats from the crashed machine are not received and the machine's 
status is marked as having failed after 2750ms. Once marked as failed on a member the marking member stops gossiping 
about the failed member. After 5500ms of not receiving heartbeats the member is removed from the membership list.

This ensures that a failure is detected in 3000ms and will be completed across the group in 5750ms. Using Google
Protocol Buffers a member message in the membership list uses X bytes. With our group size of 7 each member gossips
7*X*4 b/s and recieves a similar amount. This is near linear growth in traffic as the member count increases
since the message frequency is constant and the membership list gossiped grows linearly with the number of members.


Gossip Client and Gossip Server
-------------------------------
**Gossip Client** and **Gossip Server** communicate using UDP socktes to send membershipList and receive membershipList from machines.
Google protobufs are used to serialize/deserialize the sent/recieved DatagramPacket respectively.

**Gossip Server** listens to the multiple clients that send the membershipList. The membershipList recieved is merged with current membershipList at
the **Gossip Server** in a Synchronized manner. **Gossip Server** and **Gossip Client** both access/use the same membershipList on the machine
and so care is taken to see that membershipList is manipulated in a Synchronized way whereever appropriate.

Protos Package in src/tempest
-----------------------------

Protos package has the **Membership** java class genereted from Google Protocol buffer **Membership.proto** in protos package that are used in
serializing/deserializing the membership lists.  


Client and Server
-----------------

At a low level **Client** and **Server** communicate using java sockets and are both able to operate multi threaded. **Server**
can handle multiple simultaneous requests from clients and **Client** can send simultaneous requests to every
**Server** in it's group.

At a higher level **Client** and **Server** communicate with each other via a pair of commands. **ClientCommand** and
**ServerCommand** work together provide an extensible interface to easily implement new features between
**Client** and **Server**.

**Client** provides the ability to distribute operations to all of the machines in the group and join their
responses. **Client** is aware of all the machines in the group through **Machines** which on construction
reads the machines from the config.properties file.

**ClientCommand** provides an interface to send a request through the **Client** to a **Server** that a
corresponding **ServerCommand** can recognise and execute on the server.  Once the **ServerCommand** finishes
executing it sends a response through the **Server** to the **Client** where the **ClientCommand** processes
the response into a **CommandResponse**. **CommandResponse** is strongly typed, rather than just a string, to
facilitate easier unit testing and easier processing in the **Console**.

Currently, there are **Grep**, **Introduce**, **Leave** and **Ping** commands which result in a **Response**.

Logger
-----

**Logger** uses the logging utility included with Java for logging and Runtime.exec() to execute the standard
grep command included with most Linux and Unix systems to read logs. Currently, **Logger** provides the
ability to log and grep on two different files.  This is purely to facilitate testing for the current spec.
We think it will prove difficult to evaluate the difference between a single machine grep distributed greps if the
logs are changing underneath you.

Console
-------

**Console** uses the Cliche library to create a simple command console application which wraps the functionality
of the above described classes into a convenient utility.  You can type ?list to get a list of available commands and
exit to exit.

Tests
-----
**Logger** has the most through tests since it performs the logging and grepping. **Client** and **Server** have tests
to demonstrate that the **Ping** and **Grep** commands function across multiple servers running on the same machines
correctly.


Summary
-------

In some ways the adding of tests complicated some of the architecture but at the same time made the classes more
reusable.  The benefits of unit testing alone should justify the added classes and interfaces but knowing that
Tempest is a platform that we will be building on throughout our relationship with TheTribeHasSpoken Inc. cements it.