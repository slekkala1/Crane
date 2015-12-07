Tempest
=======

How to run?
-----------
1) On each machine go to /home/lekkala2 and use java -cp uber-tempest-1.0-SNAPSHOT.jar tempest.TempestApp to get the tempestApp running

2) Go to the machine from which you want join the membership and type ‘?list’ at the >Tempest prompt to see the function calls that can be made from Client end

3) Introducer which is VM 'fa15-cs425-g03-01.cs.illinois.edu' needs to be up for members to join the group.
Type 'mstart' or 'start-membership'(and Enter) at >Tempest to Join the membership/Gossip group and 'mstop' or 'stop-membership'(and Enter) to Leave the membership/Gossip group.  

4) Type 'mstart'(and Enter) at introducer 'fa15-cs425-g03-01.cs.illinois.edu' so others machines can join and similarly execute 'mstart' on all machines at >Tempest command prompt tomstop the group.

5) Type 'mstop'(and Enter) on the machine you want to leave.

6) Type 'gml'(and Enter) or 'get-membership-list' at >Tempest command prompt to get the latest membership list on the machine. 

7) Once mstart is done the machine is part of the worker machines.

8) To run application go to any machine and type app1/app2/app3(and enter)

9) You will see that the output will be dispalyed at one of the worker machines.

10) To fail a machine do Ctrl+C

11) To get app1 running need .csv files in SDFSFiles/quant at root.



Install
-------
1) Git Clone the repo 

2) cd cs425-mp-lekkala/tempest folder and mvn clean install

3) cd target and java -cp uber-tempest-1.0-SNAPSHOT.jar tempest.TempestApp to get the Tempest App running on your machine.


Run Tests (once you have installed)
-----------------------------------
1) cd ../../tempest-test

2) mvn clean test
