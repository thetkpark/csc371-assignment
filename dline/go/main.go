package main

import (
	"fmt"
	"strings"
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

func main() {
	n := 3
	broadcastChan := make([]chan BroadcastMessage, n)
	msgChan := make(chan IncomingMessage)

	for i:=0; i<n; i++ {
		// Spawn user/process
		broadcastChan[i] = make(chan BroadcastMessage)
		go process(broadcastChan[i], i+1)
	}
	// Spawn sequencer in another goroutine
	go sequencer(msgChan, broadcastChan)

	// To receive input from user
	for {
		var msgTypeInput, msgType string
		textMsg := ""

		fmt.Printf("\nPlease choose message type (Text, Image, Video): ")
		fmt.Scanf("%s",&msgTypeInput)
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
		msgChan <- IncomingMessage{messageType: msgType, message: textMsg}
	}
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
	fmt.Printf("Process %d: Time %v, Sequence number %d, type %s, %s\n", num, time.Now().Unix(), msg.seq, msg.messageType, msg.message)
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