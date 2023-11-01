ASSIGNMENT 3: PAXOS IMPLEMENTATION
AUTHOR: VISHAL AGARWAL
ID: a1812047

To compile: 
1. Download the files in a folder and start a terminal with pwd assigned to the directory where you download Assignment 3
2. Type make and hit enter.
To run:
3. Type make run_acceptors and hit enter. This  will  run 5 acceptors on the background and 1 on the current terminal.
4. Open a new terminal like step 1.
5. Type make run_proposers and hit enter. This will run 2 proposers on the background.
6. Make sure to follow the above steps in order. Thank you. 
7. Once a consensus is reached,  you will have to kill all processes by looking up their PID first  using the command <lsof -i :1025>  and then you should
    see  the PID in the second column, then type the command kill -9 <PID>, this will kill this server. Once done for all the port numbers(1026,1027,1028,1029,1030). You can now start to test for a new PAXOS run. 

The assignment is divided  into two main components: acceptorMembers and  proposerMembers.
The algorithm uses three phase commit. prepare(propose), accept, commit. Once a commit has happened, there is consensus reached.

acceptorMembers:
1. acts like a server that listens on a specified port. You can introduce as many acceptors using the command : java acceptorMember <port>.
<!-- Note, when we add extra acceptors, we need to  be able to tell the proposer Member to communicate to them  as  well. for which go to proposerMember.java code and add them to the run()  function. If you do this then re compilation is needed.  -->
2. each member  is now alive at  a different socket to communicate with. (I have 1025,1026,1027,1028,1029,1030 in the makefile as specified ports)

3. Multi-threading: each acceptorMember uses a new  thread  to handle concurrent  requests.Hence, there can be different proposers proposing concurrently.

4. acceptor.java: is the implementation of  what the acceptorMember should do with the incoming requests concurrently in seperate threads. hence, a clientHandler or requestHandler. 

5. Functionality: acceptors accepts the message and sends a promise: “PROMISE <identifier>” or a "REJECT <identifier>"
	Note:
    Acceptor replies to a prepare msg  in this stage: "PREPARE  <identifier>" 
    If the acceptor receives the prepare msg from the proposer with lower than or equal to
    identifier than it has already promised before, it will ignore such requests and 
    send nothing or reject msg with the current identifier. The proposer can retry with a higher identifier upon receiving the reject. 
    If the promise was made to another member, the promise msg will piggyback the value as well 
    in the message. As “PROMISE <identifier> <value>” protocol.

    Second stage: ACCEPT
    The acceptor receives "ACCEPT_REQUEST <identifier> <value>"
    "ACCEPT <identifier> <value>" is sent for accept phase when the identifier is less than or equal to the current identifier at  the acceptor. 
    Note: this time,  the  equal to condition does not  breach the  requirement like for  the  promise  phase.
    Note: if an accept request is accepted, we change the chosenValue to the new value, but if a commit had already happened, the value sent is always the committed value.

    COMMIT stage:
    at the commit stage, "President is: <value>" is sent.

proposerMembers: proposerMember.java
1.  These  members are m1, m2 and  m3. They always send the proposals to the acceptors to get the  president-ship.
2. it basically sends 6 messages to the members m4-m9  concurrently using new threads for each proposals.(Multi-threading)
3. proposer.java: This file is the implementation of what each proposal thread does.
 They connect to the acceptors at a specified  port and hostname. If the  connection  is successful, they start with the  Prepare stage(the  first phase) defined in  function PrepareMsg().
4. For all the threads  that make it after the prepare stage, they  wait until a majiority of the proposals receive a reply. When atleast 4 of them receive a reply, the other threads do  not wait  and  start with the accept phase.
5. When the accept phase starts, it can return with no value, hence stall or received some acceptedValue/chosenValue at the acceptor node. Therefore, we keep a map  with key = AcceptedValue and the value = number of times the key is the acceptedValue. if there is a consensus, a commit phase starts straightaway!, 
else we restart from the prepare phase. 
6. Commit phase: Just sends the chosenPresident value to all the nodes.  

Note, on return all the proposerMember get a file called: "<memberName>.txt" for example m1.txt  for m1 , m2.txt for m2 and m3.txt for m3.
The  file contains the final output or the member who is elected as president. 

The other output statements/backlog files are redirected to "backlog<membername>.txt", --> look at the makefile

Testing harness:
0. The easy case works and chooses m2 as their president for this run. 

1. Sending the proposal to illegal address:
output:
the host is not alive or sleeping at port 1031
the host is not alive or sleeping at port 1031
java.io.IOException: Retry after some time to send the email again to the port: 1031
        at proposer.run(proposer.java:264)
        at java.base/java.lang.Thread.run(Thread.java:833)
java.io.IOException: Retry after some time to send the email again to the port: 1031
        at proposer.run(proposer.java:264)
        at java.base/java.lang.Thread.run(Thread.java:833)
the host is not alive or sleeping at port 1031
java.io.IOException: Retry after some time to send the email again to the port: 1031
        at proposer.run(proposer.java:264)
        at java.base/java.lang.Thread.run(Thread.java:833)
DECIDED VALUE IS m2

2.What happens when a commit to m2 is already done and a new proposer sends a new request with higher identifier, 14 and name m10:
<acceptorsEnd>
PREPARE 13
13
promise was sent
ACCEPT-REQUEST 14 m10
accept was sent
COMMIT m2
President is: m2
</acceptorsEnd>
<proposersRECEIVE>President is: m2</proposersEND>

3.Unit Testing and integration testing implemented to test for basic functionality.
    Please have  a  look at  the files, m4.txt, m5.txt,....,m10.txt. They contain the output of what happens at each timestamp ordered with, 
    the previous test number 2's output in it..

4. Finally, i have introduced a random number generator,  in order to introduce variablity  to make some threads wait/sleep, when  they receive a reject in the  first stage.  This is done to make sure, we can avoid for livelocks. But as PAXOS, does not  guarantee this, the algorithm might have a slight chance  to  go  into liveness. But when the consensus/commit stage is  not reached , all the proposals will be restarted and sent again. On the second rundown, this random number generator, like in the first run would again introduce  some variability. Hence decreasing the chance for unlimited contention or livelock. 

Thank you for reading. Hope you have a wonderful rest of your day!! 

