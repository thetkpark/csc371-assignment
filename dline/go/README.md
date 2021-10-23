# Problem statement





# The proposed solution

In order to ensure that every message will be displayed to the users in the same order regardless of the delay in communication. Our solution implements the concept of total ordering in multicast ordering. Therefore, we must have a sequencer that assigns the sequence number of each message before sending multicast to the users. Each user will 2 things to keep track of. First, the local sequence number initially starts at zero. Second, the messages buffer to store the messages that are not received in the order. They must follow the same protocol such that the message will display if and only if the local sequence number + 1 is the sequence number of the incoming message. Otherwise, the message will be put in the messages buffer. Once the message has been displayed, they will check in the messages buffer again for the message that satisfies the displaying condition (local sequence number + 1 is equal to the sequence number of the message). For this solution implementation, the message will be coming from only one user and sent to the sequencer. In reality, the messages can be coming from every user but all of them have to be sent to the sequencer first. Thus, we decided to let the user input the messages first before sending those messages to the sequencer to start the simulation.

# The implement of the solution 

Our solution is implemented in Golang due to the following reasons.
   1. Golang support concurrency with Goroutine which is the higher abstraction level of thread and process
   2. It has many tools to control the concurrency when entering critical sections such as Mutex and WaitGroup.
   3. The communication between Goroutine can be done easily using Channel.

## Data Structures

```go
type MulticastMessage struct {
	seq         uint
	message     string
	messageType string
}
```

The **MulticastMessage** struct defined the data structure of the data that the sequencer sent to each user/process. It contains the message, message type, and sequence number assigned by the sequencer.

```go
type IncomingMessage struct {
	message     string
	messageType string
}
```

**IncomingMessage** struct represents the message sent from the main goroutine to the sequencer.

```go
type MessagesQueue struct {
	incomingMessage IncomingMessage
	waitDuration    time.Duration
	timestamp       time.Time
}
```

**MessagesQueue** defined the data structure of the recorded input message from the user given thorough console input. It has `waitDuration` to tell the main goroutine the interval time between this message and the previous message.

## Main Goroutine

The main function is the entry point where the Go application is compiled and run. It responsible for the following tasks

1. Printing the instruction message to the console
2. Create communication channels from the main Goroutine to the sequencer and sequencer to all of the users. The total number of Channels is 4 (3 Users).
3. Receiving message and type of message from the console
4. Spawning n number of user/process (3 is the default) and a sequencer in another Goroutine with appropriate parameters.
5. Sending all the messages to the sequencer regarding the order and time interval between each message.
6. Wait for every message are delivered and displayed at each user
7. Terminate the execution once done.

## Sequencer

The purpose of the sequencer is to assign the message with a running sequence number and send them out to each user. It has only one variable which is the global sequence number initially starting at 0. The sequencer is running in an infinite loop that waiting for a new message (IncomingMessage data structure) to come in through Channel. Once the message comes, the global sequence variable is increased by 1, and **IncomingMessage** is transformed into **MulticastMessage**. The **MulticastMessage** is sent to each user through Channels. For simulating the communication time, we spawn a `sendMulticastMessage` function another Goroutine for every message sending process. It is responsible for determining communication time to the user and using `time.Sleep()` function to simulate the communication time delay. We decided to spawn another Goroutine to do this job because of using `time.Sleep()` function would block the **IncomingMessage** from Main Goroutine to coming into the sequencer and also block the message sending process from sequencer to another user as well.

# Test scenarios





# Simulation results





# User manuals

