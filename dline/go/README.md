# The proposed solution

In order to ensure that every message will be displayed to the users in the same order regardless of the delay in communication. Our solution implements the concept of total ordering in multicast ordering. Therefore, we must have a sequencer that assigns the sequence number of each message before sending multicast to the users. Each user will 2 things to keep track of. First, the local sequence number initially starts at zero. Second, the messages buffer to store the messages that are not received in the order. They must follow the same protocol such that the message will display if and only if the local sequence number + 1 is the sequence number of the incoming message. Otherwise, the message will be put in the messages buffer. Once the message has been displayed, they will check in the messages buffer again for the message that satisfies the displaying condition (local sequence number + 1 is equal to the sequence number of the message). For this solution implementation, the message will be coming from only one user and sent to the sequencer. In reality, the messages can be coming from every user but all of them have to be sent to the sequencer first. Thus, we decided to let the user input the messages first before sending those messages to the sequencer to start the simulation.

# The implement of the solution 

Our solution is implemented in Golang due to the following reasons.
   1. Golang support concurrency with Goroutine which is the higher abstraction level of thread and process
   2. It has many tools to control the concurrency when entering critical sections such as Mutex and WaitGroup.
   3. The communication between Goroutine can be done easily using Channel.

![program structure.png](https://drive.google.com/uc?export=view&id=1Re_QNnd7IxSjsiqWOcILh8qXPCXPeCVU)

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

## User

Each user is the same function that is running in the difference Goroutine. It maintains the local sequence number in the integer variable and the array of **MulticastMessage** for the messages buffer. The user is running in an infinite loop. It waiting for an incoming message from the Channel and processes that message. When the message comes in, the message sequence number is compared with the local sequence number. If the message sequence number equals to local sequence number + 1, then the message will be displayed to the console and the local sequence number is increased by 1. After that, the buffer is checked if there is more message that satisfies the displaying condition. On the other hand, if the message sequence number is not equal to the local sequence number + 1, that message is put in the buffer. After that, the whole buffer is sorted by message sequence number for easier comparison.

# Test scenarios

According to our solution, there are 3 users (user1, user2, and user3), has only one sender that send message to sender, and there are 3 types of message (text, image, video).

Total communication time of each message = communication time of message type + random communicate time (range between 0-5 seconds). Note that communication time is randomed when sequencer send message to each user (it might be different)

1. Text (1 second + random communication time)
2. Image (5 seconds + random communication time)
3. Video (10 seconds + random communication time)

We come up with 3 test cases.

1. There is no message in buffer
   
     ![Screen Shot 2564-10-24 at 12.36.12](/Users/note/Library/Application Support/typora-user-images/Screen Shot 2564-10-24 at 12.36.12.png)
     
     ![Screen Shot 2564-10-24 at 12.34.49](/Users/note/Desktop/Screen Shot 2564-10-24 at 12.34.49.png)
     
     | Type | Message | Waiting time before<br />send next message | Communication<br />Time |
     | ---- | ------- | ------------------------------------------ | ----------------------- |
     | Text1  | 1     |  3 Second                                        | 1 second + random time |
     | Text | 2 | 4 Second | 1 second + random time |
     | Image | 1 | 4 Second | 5 second + random time |
     | Image | 2 | 5 Second | 5 second + random time |
     | Video | 1 | 4 Second | 10 second + random time |


2. There is one message in buffer

   - Sender send five message in this order

     ![Screen Shot 2564-10-24 at 12.59.03](/Users/note/Desktop/Screen Shot 2564-10-24 at 12.59.03.png)
     
     ![Screen Shot 2564-10-24 at 12.58.19](/Users/note/Desktop/Screen Shot 2564-10-24 at 12.58.19.png)
     
     | Type  | Message | Waiting time before<br />send next message | Communication<br />Time |
     | ----- | ------- | ------------------------------------------ | ----------------------- |
     | Video | 1       | 2 Second                                   | 10 second + random time |
     | Text  | 1       | 15 Second                                  | 1 second + random time  |
     | Image | 1       | 3 Second                                   | 5 second + random time  |
     | Video | 2       | 11 Second                                  | 10 second + random time |
     | Image | 2       | 2 Second                                   | 5 second + random time  |
     
     

3. There are many messages in buffer

   - Input: sender send five messgaes in the following order

     ![test-case-3-input.png](https://drive.google.com/uc?export=view&id=1DpY2wnb-UpgAmi4_Z1yGmcwuWbqOS3o9)

     

     ![test-case-3-messages.png](https://drive.google.com/uc?export=view&id=1PhBB5VDpe5nzyJJ6Ywutr8zgcCXsjmbX)
     
     | Type  | Message | Waiting time before<br />send next message / end | Communication<br />Time  |
     | ----- | ------- | ------------------------------------------------ | ------------------------ |
     | Text  | 1       | 2 seconds                                        | 1 second + random time   |
     | Text  | 2       | 2 seconds                                        | 1 second + random time   |
     | Video | 3       | 2 seconds                                        | 10 seconds + random time |
| Image | 4       | 2 seconds                                        | 5 seconds + random time  |
     | Text  | 5       | 2 seconds                                        | 1 second + random time   |

     
     

# Simulation results

These are result of the test scenarios

1. There is no message in buffer

2. There is one message in buffer

3. There are many messages in buffer

   ![test-case-3-sequencer.png](https://drive.google.com/uc?export=view&id=18e1Nf2DLaLeK2EQLUGwFjs-0OFmxp70t)

   Sequencer receives messages from main thread in the order that message was sent. Each time sequence receive the message, it will send message to all users (one message might reach users in the different time due to the random communication time).

   

   ![test-case-3-user-1.png](https://drive.google.com/uc?export=view&id=1tLsXUw5m74C6H8DGuYczfmWGuWFz7bw2)

   User1 receive and display message 1, and 2 respectively. After that it receive message 4, 5 but the sequence number that user1 is waiting for is 3. So message 4, 5 must be stored in the buffer. Then user1 receive message3 that they are waiting for, so it can display this message. After that they go into buffer and display message 4, and 5 respectively.

   

   ![test-case-3-user-2.png](https://drive.google.com/uc?export=view&id=1HmNWPCNANVio8rH-Glh8L8GdiWgckEBK)

   User2 receive an display message 1, and 2 respectively. After that it receives message 5 which sequence number is 5 but the sequence of user2 is 2. So message5 will be put int buffer. Then user2 receive message3 (sequence = 3), and sequence of user2 is 2. So message3 can be displayed. After that user2 iterate all messages in buffer which contains message5 (seq = 5 ) but it cannot be displayed because seq of user2 is 3 and 3+1 != 5. After that user2 receive and display message4 because 3+1 = 4. Finally, user2 iterate all messages in buffer and can display message5 because 4+1 = 5.



​	![test-case-3-user-3.png](https://drive.google.com/uc?export=view&id=1BT_0iMIFCFF_fuO8tebr7ofoLgFN_PhQ)

​		The order of messages that user3 receive is same as user1.



# User manuals

All you need to do is just install Go by accessing link below

https://golang.org/doc/install

After complete the installation, please close the current terminal or command line and open new one.

Then change directory to the project directory and use go run command to run simulator

```
cd dline/go
go run main.go
```



# Team Members

- 62130500209 Thanakorn Aungunchuchod
- 62130500212 Thanaphon Sombunkaeo
- 62130500230 Sethanant Pipatpakorn
