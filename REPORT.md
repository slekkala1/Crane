Tempest Architecture
====================

Overview
--------

A tempest is a like a storm but more windy and usually louder.  This is just what SpaceBookInc. needs!
We think you'll agree once you finish this brief architectural document.

**TempestApp** is implemented with the underlying failure detection in tact. It also consists of a **Crane** architecture that uses TCP sockets
 to implement stream processing, passing tuples between nodes on a cluster.

Tempest consists of a socket **Server** and **Client** that use UDP and TCP sockets to communicate, a **Logger** which performs the ability to
write logs and to grep them, and there is a convenient **Console** built on Cliche(our only third party library
in the application module). The **TempestApp** is the entry point for the application and ties all of these classes
together in to a convenient application.

The tempest module uses Maven for configuration management.  See the README.md file for more details about how to
get the application and test compiled and running using Maven.


Crane Design
--------

The **Crane** system is the stream processing tool in Tempest. It defines a system for streams to be
passed and processed at different machines in a cluster. **Crane** comes with 3 built-in applications:
a stock data processing stream, a Twitter stream, and a baseball statistics stream. More on these later.

The **Crane** system is designed so that it can support a tree structure, meaning a node will have one input and any number
of outputs. The data begins at a **Spout** and is passed
along to one or more **Bolts**, where the tuples of data can be processed in a variety of ways. A **Bolt** can either
send the data to another **Bolt**, or collect it as output if it is the final processing step. This tree topology is defined in the
**Topologies** class. The **Topologies** class creates a TCP message that is relayed to the introducer of the cluster, where each
bolt is assigned to a node. The introducer will relay these assignments out to other nodes so that data can be sent along the correct path.

Spouts are defined for each application, but all implement the BaseSpout interface. This ensures there is a retrieveTuples method that pulls
the data either from files or some other source. Spouts are always mapped to the introducer since the tree structure ensures there is only ever
one spout per application. The specific spout type for an application is defined in Topologies.

Bolts in **Crane** are all accessed via the common class, BaseBolt, but are defined for each individual application. They have a method that performs
an operation on a tuple that can be set to filter tuples, join a tuple with another dataset, transform a tuple. After being processed by a bolt, the data
is either sent to the next bolt or kept at that machine as output.

The **Crane** system was built on top of the previous Tempest app, so it utilizes the membership list and failure detection. After a machine
has joined the membership list, it will automatically be a candidate for a bolt in the **Crane** topology. The bolt machines are chosen at random. Upon failure,
the introducer/spout will be alerted and will restart the stream if needed.

The first application in **Crane** is a stock data processing stream. As input, it takes stock data tuples read from csv files. It runs them first through a filter
bolt to find only stock tuples with a closing price higher than the opening price, and then through a transform bolt to trim the rest of the tuple and output the
id, name, and change in price of the stock.

The second application is the Twitter stream. It connects to the Twitter API to pull data and run tuples through filter and transform bolts.
It's output is the name and text of popular tweets, where a popular tweet is defined as one that has at least 5 times as many favorites as the
tweeter has followers.

The last application processes a stream of baseball data. It finds all players in the dataset that have hit at least 10 homeruns in a season, and outputs
their name along with the homerun total. Their name is found using a join bolt - players names are stored as ids in the streaming data, so this
is joined with a dataset that contains their real name and id.

DATA:

For future improvements, the spout should be allowed to map to any machince. That way the introducer does not always have to do all initial data pulling.
Additionally, the data transfer between VMs could be parallelized better by allowing for additional machines to become bolts if one is moving
slower.


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
Protocol Buffers a member message in the membership list uses 29 bytes. With our group size of 7 each member gossips
7members*29bytes*4gossips/second = 812B/s, not counting overhead from UDP, and recieves a similar amount. This is 
near linear growth in traffic as the member count increases since the message frequency is 
constant and the membership list gossiped grows linearly with the number of members. However, the failure detection
rate will need to be increased logarithmicly with the increase in membership size to maintain the same
false detection rate.

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

Additionally, **Command.proto** contains the structures used to generate java classes for all other command's messaging.


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
Tempest is a platform that we will be building on throughout our relationship with CallMeIshmaelInc. cements it.