package main

import (
	"fmt"
	"github.com/pterm/pterm"
	"math/rand"
	"os"
	"sort"
	"strings"
	"sync"
	"time"
)

// BroadcastMessage defined the data structure that the sequencer sent to each user/process
type BroadcastMessage struct {
	seq uint
	message string
	messageType string
}
// IncomingMessage represent the message from the user received via console that later will be sent to sequencer
type IncomingMessage struct {
	message string
	messageType string
}
// MessagesQueue defined the data structure of the recorded input message from the user via console input.
// It will be used later when the user stop typing the input message.
type MessagesQueue struct {
	incomingMessage IncomingMessage
	waitDuration time.Duration
	timestamp time.Time
}

// Defined the constants
const(
	Text string = "text"
	Image string = "image"
	Video string = "video"
	n int = 3
)

// WaitGroup is used to stop the main goroutine to exiting early and wait for all process/user and sequencer to get their works done.
var wg sync.WaitGroup
// Mutex is used to prevent two goroutine to enter critical section at the same time. Also helped to display the output that not overlap.
var m sync.Mutex
// outputs is used to store all the output in string. It will be later render by Ptem library to update the text area.
var outputs []string

// Main is the main goroutine and responsible for spawn other goroutine and get input from the user
func main() {
	outputs = make([]string, n+2) // create slides of outputs that for each user/process + main thread and sequencer
	outputs[0] = pterm.DefaultSection.WithLevel(1).Sprintln("Main")
	outputs[1] = pterm.DefaultSection.WithLevel(2).Sprintln("Sequencer")
	broadcastChan := make([]chan BroadcastMessage, n) // create n number of channel for communication between sequencer and each user/process
	msgChan := make(chan IncomingMessage) // create a channel of communication for main goroutine to send message to sequencer in another goroutine
	var messages []MessagesQueue

	// Print the welcoming message and instruction to the user
	pterm.PrintDebugMessages = true
	bt, _ := pterm.DefaultBigText.WithLetters(pterm.NewLettersFromStringWithStyle("D-LINE", pterm.NewStyle(pterm.FgLightBlue))).Srender()
	pterm.DefaultCenter.Print(bt)
	pterm.DefaultBasicText.Println(`This is a visualization of how D-LINE deal with messages that are not received in order.
Please enter the messages that will be used in visualization. It will be played in same order and interval.
Note that the time that each messages are sent to sequencer is time you used before submit another message.
There are 3 types of message you can choose from. 
Each of them has the difference communication time delay from sequencer to the user.
- Text (1 second + random communication time)
- Image (5 seconds + random communication time)
- Video. (10 seconds + random communication time)
The random communication time is calculated when the sequencer forward message to each user. (range between 0-5 seconds)`)
	// Receive input messages from user
	for {
		var msgTypeInput, msgType, textMsg string
		correctType := true
		fmt.Printf("\nPlease choose message type (Text, Image, Video) or 'End' to stop: ")
		fmt.Scanf("%s",&msgTypeInput)
		if strings.ToLower(msgTypeInput) == "end" {
			// If the user have not input any message -> not let them out
			if len(messages) == 0 {
				pterm.Error.Println("Please enter at least one message")
			}
			break
		}
		switch strings.ToLower(msgTypeInput) {
		case Text:
			msgType = Text
			fmt.Printf("Please enter text message: ")
		case Image:
			msgType = Image
			fmt.Printf("Please enter image title: ")
		case Video:
			msgType = Video
			fmt.Printf("Please enter video title: ")
		default:
			correctType = false
			pterm.Error.Println("Please enter the correct message type")
		}
		if !correctType {
			continue
		}
		fmt.Scanf("%s", &textMsg)

		// Calculate a interval between message
		var waitDuration int64
		if len(messages) != 0 {
			waitDuration = time.Now().UnixNano() - messages[len(messages)-1].timestamp.UnixNano()
		}
		// Put input message in MessagesQueue data structure
		messages = append(messages, MessagesQueue{
			incomingMessage: IncomingMessage{
				message:     textMsg,
				messageType: msgType,
			},
			waitDuration: time.Duration(waitDuration),
			timestamp:    time.Now(),
		})
	}
	// Print out all the input messages
	for i, msg := range messages {
		pterm.DefaultCenter.Printf("\nType %s, %s", msg.incomingMessage.messageType, msg.incomingMessage.message)
		if i < len(messages) - 1 {
			pterm.DefaultCenter.Printf("\n|\n| %v seconds\n∨", messages[i+1].waitDuration.Round(time.Second).Seconds())
		}
	}
	// Ask for user confirmation before continue
	var isContinue string
	fmt.Print("\nYour messages will be sent to sequencer in the order as shown above. Continue? [Y]es, [N]o: ")
	fmt.Scanf("%s", &isContinue)
	switch strings.ToLower(isContinue) {
	case "y", "yes":
	default:
		os.Exit(0)
	}
	fmt.Println("The visualization will start now...")

	// Create updatable output console area
	area, _ := pterm.DefaultArea.Start()
	for i:=0; i<n; i++ {
		// Spawn the user/process
		broadcastChan[i] = make(chan BroadcastMessage)
		outputs[i+2] = pterm.DefaultSection.WithLevel(i+1+2).Sprintln("User ", i+1)
		go process(broadcastChan[i], i+2, area)
	}
	// Spawn the sequencer
	go sequencer(msgChan, broadcastChan, area)

	// Sending message one by one to sequencer with time delay
	for _, message := range messages {
		wg.Add(n)
		time.Sleep(message.waitDuration)
		msgChan <- message.incomingMessage
		printMessage(pterm.Success.Sprintfln("SENT MESSAGE, Timestamp %v, Type %s, %s", time.Now().Unix(), message.incomingMessage.messageType, message.incomingMessage.message), 0, area)
	}
	wg.Wait() // Wait for all goroutine to finish before exit
}

// process represent the user in D-LINE
func process(in <-chan BroadcastMessage, num int, area *pterm.AreaPrinter) {
	var localSeq uint = 0 // Local sequence number of the user/process
	var buffer []BroadcastMessage // Buffer for un-order message

	// Run process in infinite loop
	for {
		// Wait for a message to come in through channel
		msg := <- in
		// If it's a expected message -> print it out
		if localSeq + 1 == msg.seq {
			printMessage(pterm.Success.Sprintf("RECEIVED MESSAGE AND DISPLAY, Timestamp %v, Seq %d, Type %s, %s\n", time.Now().Unix(), msg.seq, msg.messageType, msg.message), num, area)
			localSeq++
			wg.Done()

			// Look at the buffer to see if there is next message
			for len(buffer) > 0 && localSeq + 1 == buffer[0].seq {
				printMessage(pterm.Success.Sprintf("DISPLAY FROM BUFFER, Timestamp %v, Seq %d, Type %s, %s\n", time.Now().Unix(), buffer[0].seq, buffer[0].messageType, buffer[0].message), num, area)
				buffer = buffer[1:]
				sortBufferMessages(buffer)
				localSeq++
				wg.Done()
			}
		} else {
			// Not the expected message -> put in buffer and sort by sequence number
			printMessage(pterm.Info.Sprintf("RECEIVED MESSAGE AND ADDED TO BUFFER, Timestamp %v, Seq %d, Type %s, %s\n", time.Now().Unix(), msg.seq, msg.messageType, msg.message), num, area)
			buffer = append(buffer, msg)
			sortBufferMessages(buffer)
		}
	}
}

// sequencer represent the central server that order all the messages
func sequencer(incomingMsg <-chan IncomingMessage, broadcast []chan BroadcastMessage, area *pterm.AreaPrinter) {
	var seq uint = 0

	for {
		// Wait for message from the main goroutine (That user inputs)
		msg := <-incomingMsg
		seq++
		printMessage(pterm.Info.Sprintfln("RECEIVED MESSAGE, Timestamp %v, Type %s, %s", time.Now().Unix(), msg.messageType, msg.message), 1, area)
		for i, process := range broadcast{
			// Sending message with sequence number to every user through channel
			go sendBroadcastMessage(process, BroadcastMessage{
				seq:     seq,
				message: msg.message,
				messageType: msg.messageType,
			})
			printMessage(pterm.Success.Sprintfln("SENT MESSAGE TO USER %d, Timestamp %v, Seq %d, Type %s, %s", i+1, time.Now().Unix(), seq, msg.messageType, msg.message), 1, area)
		}
	}
}

// printMessage is a utility function to print the new message to output without overlapping
func printMessage(msg string, num int, area *pterm.AreaPrinter) {
	// Use mutex to lock and unlock before enter critical section
	m.Lock()
	outputs[num] += msg
	area.Update(getAllSectionOutputString())
	m.Unlock()
}

// getAllSectionOutputString concatenate all the string in each section from outputs slice
func getAllSectionOutputString() string {
	totalOutText := ""
	for _, output := range outputs {
		totalOutText += output
	}
	return totalOutText
}

// sortBufferMessages sort the order of the messages in buffer by its sequence number in ascending format
func sortBufferMessages(messages []BroadcastMessage) {
	sort.Slice(messages, func(i, j int) bool {
		return messages[i].seq < messages[j].seq
	})
}

// sendBroadcastMessage is used to simulate sending message from sequencer to user
// It used time.Sleep(duration) to simulate delay in communication
func sendBroadcastMessage(c chan<- BroadcastMessage, msg BroadcastMessage) {
	var sleepDuration int64
	switch msg.messageType {
	case Text:
		sleepDuration = time.Second.Nanoseconds()
	case Image:
		sleepDuration = (time.Second * 5).Nanoseconds()
	case Video:
		sleepDuration = (time.Second * 10).Nanoseconds()
	}
	rand.Seed(time.Now().UnixNano())
	sleepDuration += rand.Int63n(5) * 1000000000
	time.Sleep(time.Duration(sleepDuration))
	c <- msg
}