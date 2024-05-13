/*
 * PortClientUDP.java
 * 
 * 	simple udp client, send request, prints answer
 */
import java.io.*;
import java.net.*;

public class PortClientUDP {
	
	public static void main (String args[]) throws IOException {
		DatagramSocket socket;	
        socket = new DatagramSocket();

        String send = "Group X";

		byte[] buffer = send.getBytes();
		InetAddress address = InetAddress.getByName("localhost");
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 9085);
		socket.send(packet);

		// get response
        buffer = new byte[256];
		packet = new DatagramPacket(buffer, buffer.length);
		socket.receive(packet);

		// display response
		String received = new String(packet.getData());
		System.out.println("Response: " + received);

		socket.close();				
	}
}

