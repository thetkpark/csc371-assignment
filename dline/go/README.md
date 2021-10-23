# Problem statement





# The proposed solution

In order to ensure that every message will be displayed to the users in the same order regardless of the delay in communication. Our solution implements the concept of total ordering in multicast ordering. Therefore, we must have a sequencer that assigns the sequence number of each message before sending multicast to the users. Each user will 2 things to keep track of. First, the local sequence number initially starts at zero. Second, the messages buffer to store the messages that are not received in the order. They must follow the same protocol such that the message will display if and only if the local sequence number + 1 is the sequence number of the incoming message. Otherwise, the message will be put in the messages buffer. Once the message has been displayed, they will check in the messages buffer again for the message that satisfies the displaying condition (local sequence number + 1 is equal to the sequence number of the message). For this solution implementation, the message will be coming from only one user and sent to the sequencer. In reality, the messages can be coming from every user but all of them have to be sent to the sequencer first. Thus, we decided to let the user input the messages first before sending those messages to the sequencer to start the simulation.

# The implement of the solution 

Our solution is implemented in Golang due to these following reasons.

1. Golang was designed to support concurrency with Goroutine which is the higher abstraction level of thread and process
2. It has many many tools to control the concurrency when entering critical section such as Mutex and WaitGroup.
3. The communication between Goroutine can be done easily using Channel.

## Data Structures

```go
type MulticastMessage struct {
	seq         uint
	message     string
	messageType string
}
```

The **BroadcastMessage** struct defined the data structure of the data that the sequencer sent to each user/process. It contains message, message type, and sequence number assigned by the sequencer.

```go
type IncomingMessage struct {
	message     string
	messageType string
}
```

**IncomingMessage** struct represent the message that sent from main goroutine to the sequencer.

```go
type MessagesQueue struct {
	incomingMessage IncomingMessage
	waitDuration    time.Duration
	timestamp       time.Time
}
```

**MessagesQueue** defined the data structure of the recorded input message from the user given thorough console input. It has `waitDuration` to tell the main goroutine the interval time between this message and the previous message.

## Main Goroutine

The main function is the entry point where the Go application is compiled and run. It responsible for these following tasks

1. Printing the instruction message to the console
2. Create communication channels from main Goroutine to the sequencer and sequencer to all of the user. Total number of channel is 4 (3 User).
3. Receiving message and type of message from console
4. Spawning n number of user (3 is default) and a sequencer in another Goroutine with appropriate parameters.
5. Sending all the messages to sequencer regarding the order and time interval between each message.
6. Wait for every messages are delivered and displayed at each user
7. Terminate the execution once done.

# Test scenarios





# Simulation results





# User manuals

