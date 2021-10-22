# Problem statement





# The proposed solution

In order to ensure that the every messages will be display to the users at the same order regardless of the delay in communication. Our solution implement the concept of total ordering in multicast ordering. Therefore, we must have the sequencer that assign the sequence number of each message before sending multicase to each user. Each user will 2 things to keep track. First, the local sequence number which initially start at zero. Second, the messages buffer to store the messages that are not received in the order. They must follow the same protocol such that the message will display if and only if the local sequence number + 1 is the sequence number of the incoming message. Otherwise, the message will be put in the messages buffer. When the message has displayed, they will check in the messages buffer again for the message that satisfy the displaying condition (local sequence number + 1 is equal sequence number of the message). For this solution, the message will be coming from only one user and sent to the sequencer. In reality, the messages can be coming from every user but all of them have to be sent to the sequencer first. Thus, we decided to let the user input the messages first before sending those messages to sequencer to start the simulation.



# The implement of the solution in Golang





# Test scenarios





# Simulation results





# User manuals