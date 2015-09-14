=======
Tempest
======


How to run?
1) On each machine, run /opt/tempest/run-tempest.sh to start the Server and Client, the corresponding vmx.log file is already on each machine
2) Go to the machine from which you want to make the grep distributed call and type ‘?list’ at the >Tempest prompt to see the function calls that can be made from Client end
3) grep-all world-- will grep all the VM logs for world,
grep-all “-c world”  will get the word count of world in all machines. Please provide the necessary pattern and options in parenthesis. It will automatically grep the pre-existing vm1.log, vm2.log and so on files on the VMs.
4) gatf output.log world-- will grep all the VM logs to output.log file on Client end. This can be used to check the output generated from the log querying program. Similarly any other pattern can be provided, for example: gatf output.log “-c world”. Note: output files are written to /opt/tempest/

Install
1) Git Clone the repo 
2) cd cs425-mp-lekkala-morrow/tempest folder and mvn clean install 
2) cd target and java -jar uber-tempest-1.0-SNAPSHOT.jar to get the Tempest App running on your machine

Run Tests (once you have installed)
1) cd ../../tempest-test
2) mvn clean test




