all: acceptor.class acceptorMember.class proposer.class proposerMember.class

acceptor.class: acceptor.java
	javac acceptor.java

acceptorMember.class: acceptorMember.java
	javac acceptorMember.java

proposer.class: proposer.java
	javac proposer.java

proposerMember.class: proposerMember.java
	javac proposerMember.java

run_acceptors:
	java acceptorMember 1025 > backlogm4.txt &
	java acceptorMember 1026 > backlogm5.txt &
	java acceptorMember 1027 > backlogm6.txt &
	java acceptorMember 1028 > backlogm7.txt &
	java acceptorMember 1029 > backlogm8.txt &
	java acceptorMember 1030 > backlogm9.txt 

run_proposers:
	java proposerMember m1  >backlogm1.txt &
	java proposerMember m2  >backlogm2.txt &
	java proposerMember m3 >backlogm3.txt 