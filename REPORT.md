Tempest Architecture
====================

Overview
--------

A tempest is a like a storm but more windy and usually louder.  This is just what You're Fired! Inc. needs!
We think you'll agree once you finish this brief architectural document.

Tempest consists of a socket **Client** and **Server** for communication, a **Logger** which performs the ability to
write logs and to grep them, and there is a convenient **Console** built on Cliche(our only third party library 
in the application module). The **TempestApp** is the entry point for the application and ties all of these classes
together in to a convenient application.
  
There are two modules in the git repository: an application module (tempest) and a test module (tempest-test).
The test module provides a suite of junit test to verify that the most important features of Tempest are
fully functional. The application module provides the fully functioning Tempest application.
 
Both modules use Maven for configuration management.  See the README.md file for more details about how to
get the application and test compiled and running using Maven.

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
executing it sends a response through the **Server** to the **Client** where the **ClientCommand processes 
the response into a **CommandResponse**. **CommandResponse** is strongly typed, rather than just a string, to 
facilitate easier unit testing and easier processing in the **Console**.

Currently, there are **Grep** and **Ping** commands which both result in a **Response**. Only **Grep** was 
required by the spec; however, **Ping** was very useful to get things going and it is handy to use from **Console** 
to figure out if a machine is available.

Logger
------

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

Performance
-----------


Summary
-------

In some ways the adding of tests complicated some of the architecture but at the same time made the classes more 
reusable.  The benefits of unit testing alone should justify the added classes and interfaces but knowing that 
Tempest is a platform that we will be building on throughout our relationship with You're Fired!, Inx. cements it.