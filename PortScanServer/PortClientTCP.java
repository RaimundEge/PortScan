/*
 * PortClientTCP.java
 *
 *  	simple tcp client, send request, prints answer
 * 
 */
import java.io.*;
import java.net.*;

public class PortClientTCP {
	
	public static void main (String args[]) throws IOException {
		String sentence = "Group X";
		String modifiedSentence;

		Socket socket = new Socket("localhost", 9042);
		PrintStream out = new PrintStream(socket.getOutputStream());
		BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		out.println(sentence);
		modifiedSentence = inFromServer.readLine();
		System.out.println("FROM SERVER: " + modifiedSentence);
		socket.close();		
	}
}

