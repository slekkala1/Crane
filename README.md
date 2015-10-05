Tempest
=======

How to run?
-----------
1) On each machine, run /opt/tempest/run-tempest.sh to start the Server and Client

2) Go to the machine from which you want join the membership and type ‘?list’ at the >Tempest prompt to see the function calls that can be made from Client end

3) Introducer which is VM 'fa15-cs425-g03-01.cs.illinois.edu' needs to be up for members to join the group.
Type 'sm' or 'start-membership'  at >Tempest to Join the membership/Gossip group and 'stme' or 'stop-membership' to Leave the membership/Gossip group.  

4) 'sm' at introducer 'fa15-cs425-g03-01.cs.illinois.edu' so others machines can join and execute 'sm' on all machines at >Tempest command prompt to join the group.

5) 'stme' on the machine you want to leave.

6) 'gml' or 'get-membership-list' at >Tempest command prompt to get the latest membership list on the machine. 


Install
-------
1) Git Clone the repo 

2) cd cs425-mp-lekkala-morrow/tempest folder and mvn clean install 

3) cd target and java -jar uber-tempest-1.0-SNAPSHOT.jar to get the Tempest App running on your machine

Run Tests (once you have installed)
-----------------------------------
1) cd ../../tempest-test

2) mvn clean test
