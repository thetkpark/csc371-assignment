package main

import (
	"fmt"
	"github.com/pterm/pterm"
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
	outputs = make([]string, n) // create slides of outputs that for each user/process
	broadcastChan := make([]chan BroadcastMessage, n) // create n number of channel for communication between sequencer and each user/process
	msgChan := make(chan IncomingMessage) // create a channel of communication for main goroutine to send message to sequencer in another goroutine
	var messages []MessagesQueue

	pterm.PrintDebugMessages = true
	bt, _ := pterm.DefaultBigText.WithLetters(pterm.NewLettersFromString("D-LINE")).Srender()
	pterm.DefaultCenter.Print(bt)
	pterm.DefaultCenter.WithCenterEachLineSeparately().Println(`This is a visualization of how D-LINE would deal with messages that are not received in order
Please enter the messages that will be used in simulation. 
Note that time that each messages are sent to sequencer is time you used before submit another message`)
	// Receive input from user
	for {
		var msgTypeInput, msgType, textMsg string
		fmt.Printf("\nPlease choose message type (Text, Image, Video) or 'End' to stop: ")
		fmt.Scanf("%s",&msgTypeInput)
		if strings.ToLower(msgTypeInput) == "end" {
			fmt.Printf("Your messages is saved and the visualization will start now...")
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
		}
		fmt.Scanf("%s", &textMsg)

		var waitDuration int64
		if len(messages) != 0 {
			waitDuration = time.Now().UnixNano() - messages[len(messages)-1].timestamp.UnixNano()
		}
		messages = append(messages, MessagesQueue{
			incomingMessage: IncomingMessage{
				message:     textMsg,
				messageType: msgType,
			},
			waitDuration: time.Duration(waitDuration),
			timestamp:    time.Now(),
		})
	}
	// Print out all the messages
	for i, msg := range messages {
		pterm.DefaultCenter.Printf("\nType %s, %s", msg.incomingMessage.messageType, msg.incomingMessage.message)
		if i < len(messages) - 1 {
			pterm.DefaultCenter.Printf("\n|\n| %v seconds\n∨", messages[i+1].waitDuration.Round(time.Second).Seconds())
		}
	}

	area, _ := pterm.DefaultArea.Start()

	for i:=0; i<n; i++ {
		// Spawn user/process
		broadcastChan[i] = make(chan BroadcastMessage)
		outputs[i] = pterm.DefaultSection.WithLevel(i+1).Sprintln("User ", i+1)
		go process(broadcastChan[i], i, area)
	}
	// Spawn sequencer in another goroutine
	go sequencer(msgChan, broadcastChan)

	for _, message := range messages {
		wg.Add(n)
		time.Sleep(message.waitDuration)
		msgChan <- message.incomingMessage
	}
	wg.Wait()
}


func process(in <-chan BroadcastMessage, num int, area *pterm.AreaPrinter) {
	var localSeq uint = 0
	var buffer []BroadcastMessage

	// Run process in infinite loop
	for {
		// message come in
		msg := <- in
		// If it's a expected message -> print it out
		if localSeq + 1 == msg.seq {
			printMessage(pterm.Info.Sprintf("DISPLAY Time %v, Seq %d, type %s, %s\n", time.Now().Unix(), msg.seq, msg.messageType, msg.message), num, area)
			localSeq++
			wg.Done()

			// Look at the buffer to see if there is next message
			for len(buffer) > 0 && localSeq + 1 == buffer[0].seq {
				printMessage(pterm.Info.Sprintf("DISPLAY FROM BUFFER Time %v, Seq %d, type %s, %s\n", time.Now().Unix(), buffer[0].seq, buffer[0].messageType, buffer[0].message), num, area)
				buffer = buffer[1:]
				sortBufferMessages(buffer)
				localSeq++
				wg.Done()
			}
		} else {
			// Not the expected message -> put in buffer
			printMessage(pterm.Debug.Sprintf("ADD TO BUFFER Time %v, Seq %d, type %s, %s\n", time.Now().Unix(), msg.seq, msg.messageType, msg.message), num, area)
			buffer = append(buffer, msg)
			sortBufferMessages(buffer)
		}
	}
}

func sequencer(incomingMsg <-chan IncomingMessage, broadcast []chan BroadcastMessage) {
	var seq uint = 0

	for {
		msg := <-incomingMsg
		seq++
		for _, process := range broadcast{
			go sendBroadcastMessage(process, BroadcastMessage{
				seq:     seq,
				message: msg.message,
				messageType: msg.messageType,
			})
		}
	}
}

func printMessage(msg string, num int, area *pterm.AreaPrinter) {
	m.Lock()
	outputs[num] += msg
	area.Update(getAllSectionOutputString())
	m.Unlock()
}

func getAllSectionOutputString() string {
	totalOutText := ""
	for _, output := range outputs {
		totalOutText += output
	}
	return totalOutText
}

func sortBufferMessages(messages []BroadcastMessage) {
	sort.Slice(messages, func(i, j int) bool {
		return messages[i].seq < messages[j].seq
	})
}

func sendBroadcastMessage(c chan<- BroadcastMessage, msg BroadcastMessage) {
	switch msg.messageType {
	case Text:
		time.Sleep(time.Second * 1)
	case Image:
		time.Sleep(time.Second * 5)
	case Video:
		time.Sleep(time.Second * 10)
	}
	c <- msg
}