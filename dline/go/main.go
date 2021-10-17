package main

import (
	"fmt"
	"strings"
	"sync"
	"time"
)

const(
	Text string = "text"
	Image string = "image"
	Video string = "video"
)

type BroadcastMessage struct {
	seq uint
	message string
	messageType string
}

type IncomingMessage struct {
	message string
	messageType string
}

type MessagesQueue struct {
	incomingMessage IncomingMessage
	waitDuration time.Duration
	timestamp time.Time
}

var wg sync.WaitGroup

func main() {
	n := 3
	broadcastChan := make([]chan BroadcastMessage, n)
	msgChan := make(chan IncomingMessage)
	var messages []MessagesQueue

	for i:=0; i<n; i++ {
		// Spawn user/process
		broadcastChan[i] = make(chan BroadcastMessage)
		go process(broadcastChan[i], i+1)
	}
	// Spawn sequencer in another goroutine
	go sequencer(msgChan, broadcastChan)

	fmt.Println("This is a visualization of how d-line would deal with messages that are not received in order")
	fmt.Println("Please enter the messages that will be used in simulation. Note that the interval of each message is the interval that you submit the message")
	// To receive input from user
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

	for _, message := range messages {
		wg.Add(n)
		time.Sleep(message.waitDuration)
		msgChan <- message.incomingMessage
	}
	wg.Wait()
}


func process(in <-chan BroadcastMessage, num int) {
	var localSeq uint = 0
	var buffer []BroadcastMessage

	// Run process in infinite loop
	for {
		// message come in
		msg := <- in
		// If it's a expected message -> print it out
		if localSeq + 1 == msg.seq {
			printMessage(msg, num)
			localSeq++

			// Look at the buffer to see if there is next message
			for len(buffer) > 0 && localSeq + 1 == buffer[0].seq {
				printMessage(buffer[0], num)
				buffer = buffer[1:]
				localSeq++
			}
		} else {
			// Not the expected message -> put in buffer
			buffer = append(buffer, msg)
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

func printMessage(msg BroadcastMessage, num int) {
	color := "\033[31m"
	switch num {
	case 1:
		color = "\033[33m"
	case 2:
		color = "\033[34m"
	}
	fmt.Printf("\n%sProcess %d: Time %v, Sequence number %d, type %s, %s", color, num, time.Now().Unix(), msg.seq, msg.messageType, msg.message)
	wg.Done()
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