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

	// Spawn sequencer in another goroutine
	for i:=0; i<n; i++ {
		// Spawn user/process
		broadcastChan[i] = make(chan BroadcastMessage)
		go process(broadcastChan[i], i+1)
	}
	go sequencer(msgChan, broadcastChan)

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
		time.Sleep(time.Second)
	}
}


func process(in <-chan BroadcastMessage, num int) {
	var localSeq uint = 0
	var buffer []BroadcastMessage

	for {
		msg := <- in
		if localSeq + 1 == msg.seq {
			printMessage(msg, num)
			localSeq++

			// Look at the buffer
			for len(buffer) > 0 && localSeq + 1 == buffer[0].seq {
				printMessage(msg, num)
				buffer = buffer[1:]
				localSeq++
			}
		} else {
			buffer = append(buffer, msg)
		}
	}
}

func printMessage(msg BroadcastMessage, num int) {
	fmt.Printf("Process %d: Sequence number %d, type %s, %s\n", num, msg.seq, msg.messageType, msg.message)
}

func sequencer(incomingMsg <-chan IncomingMessage, broadcast []chan BroadcastMessage) {
	var seq uint = 0

	for {
		msg := <-incomingMsg
		seq++
		for _, process := range broadcast{
			process <- BroadcastMessage{
				seq:     seq,
				message: msg.message,
				messageType: msg.messageType,
			}
		}
	}
}