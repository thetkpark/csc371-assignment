# Problem statement





# The proposed solution

In order to ensure that every message will be displayed to the users in the same order regardless of the delay in communication. Our solution implements the concept of total ordering in multicast ordering. Therefore, we must have a sequencer that assigns the sequence number of each message before sending multicase to the users. Each user will 2 things to keep track of. First, the local sequence number initially starts at zero. Second, the messages buffer to store the messages that are not received in the order. They must follow the same protocol such that the message will display if and only if the local sequence number + 1 is the sequence number of the incoming message. Otherwise, the message will be put in the messages buffer. Once the message has been displayed, they will check in the messages buffer again for the message that satisfies the displaying condition (local sequence number + 1 is equal to the sequence number of the message). For this solution implementation, the message will be coming from only one user and sent to the sequencer. In reality, the messages can be coming from every user but all of them have to be sent to the sequencer first. Thus, we decided to let the user input the messages first before sending those messages to the sequencer to start the simulation.



# The implement of the solution in Golang





# Test scenarios





# Simulation results





# User manuals