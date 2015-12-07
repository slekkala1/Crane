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

Client and Server
-------------------------------
**Client** and **Gossip Server** communicate using TCP socktes to send tuples and data between machines from machines.
Google protobufs are used to serialize/deserialize the sent/recieved DatagramPacket respectively.

Protos Package in src/tempest
-----------------------------

Protos package has the **Membership** java class genereted from Google Protocol buffer **Membership.proto** in protos package that are used in
serializing/deserializing the membership lists.

Additionally, **Command.proto** contains the structures used to generate java classes for all other command's messaging.

Console
-------

**Console** uses the Cliche library to create a simple command console application which wraps the functionality
of the above described classes into a convenient utility.  You can type ?list to get a list of available commands and
exit to exit.


Summary
-------

The previous MPs were extremely helpful in developing Crane. The membership list and failure were crutial to the application, and
the SDFS could have been better utilized to store outputs and datasets for joins.