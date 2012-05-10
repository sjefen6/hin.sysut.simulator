package org.hikst.Simulator;

public class AliveMessenger extends Thread {
	
	public AliveMessenger(){
		super("AliveMessenger"); // Initialize thread.
		start(); // Start this Thread
	}
	
	public void run() {
		// Update Last_Seen_TS in DB
		// www.showmyip.com/simple/
	}
}
