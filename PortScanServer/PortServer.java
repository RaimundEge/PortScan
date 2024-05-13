/*
 * PortServer.java
 *    
 *	in threads/loops:
 * 		listens on random UDP port
 * 		gets request from client with name, creates key, stores and sends it
 *      
 *              listens on random TCP port
 * 		gets request from client, looks up key, encrypts message (RC4) and sends it
 */

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

class KeyStore {

    static HashMap<String, SecretKey> store = new HashMap<>();
    static Random rand = new Random();
}

class PortServerUDP extends Thread {

    public void run() {
        while (true) {
            int port = 9000 + KeyStore.rand.nextInt(101);
            try (DatagramSocket socket = new DatagramSocket(port)) {

                System.out.println("Starting UDP server on port: " + port);

                // request must be in form: Group N
                byte[] requestBytes = new byte[8];
                // prepare record for logrecords DB
                StringBuffer record = new StringBuffer();
                // receive request
                DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length);
                socket.receive(packet);
                String groupName = new String(packet.getData()).trim();

                String timeStamp = new SimpleDateFormat("yy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());
                System.out.print(timeStamp + ": UDP request: " + groupName);
                // check for valid group name
                boolean nameOK = false;
                switch (groupName) {
                case "Group 1":
                case "Group 2":
                case "Group 3":
                case "Group 4":
                case "Group 5":
                case "Group 6":
                case "Group 7":
                case "Group 8":
                case "Group 9":
                case "group 1":
                case "group 2":
                case "group 3":
                case "group 4":
                case "group 5":
                case "group 6":
                case "group 7":
                case "group 8":
                case "group 9":
                case "group X":
                case "group Y":
                    nameOK = true;
                }

                // construct response
                byte[] responseBytes = null;

                if (!nameOK) {
                    String response = "Error: group name not recognized >" + groupName + "<";
                    record.append(response);
                    responseBytes = response.getBytes();
                    System.out.println(", response: " + response);
                    groupName = "unknown";
                } else {
                    // generate key, store and send to the client
                    KeyGenerator keyGen = KeyGenerator.getInstance("RC4");
                    SecretKey secretKey = keyGen.generateKey();
                    if (!groupName.equals("group X")) {
                        KeyStore.store.put(groupName, secretKey); 
                    }                  
                    responseBytes = secretKey.getEncoded();
                    record.append(DB.bytesToHex(responseBytes));
                    System.out.println(", response: key sent, size: " + responseBytes.length);
                }
                // log what is sent
                DB.db.write(groupName, "UDP", packet.getAddress().toString(), port,  record.toString());
                // send repsonse packet
                packet = new DatagramPacket(responseBytes, responseBytes.length, packet.getAddress(), packet.getPort());
                socket.send(packet);
                socket.close();

            } catch (Exception e) {
                System.out.println("1. Exception caught: " + e);
                e.printStackTrace();
            }
        }
    }
}

class PortServerTCP extends Thread {

    public void run() {
        while (true) {
            int port = 9000 + KeyStore.rand.nextInt(101);
            ServerSocket sock = null;
            try {
                sock = new ServerSocket(port);
                System.out.println("Starting TCP server on port: " + port);

                new FeedClient(sock.accept(), port).start();
                sock.close();
            } catch (Exception e) {
                System.out.println("2. Exception caught: " + e);
                e.printStackTrace();
            }
            try {
                sock.close();
            } catch (Exception e) {
                System.out.println("3. Exception caught: " + e);
                e.printStackTrace();
            }
            // System.out.println("TCP server done with port: " + port);
        }
    }
}

class FeedClient extends Thread {

    Socket sock;
    int port;

    FeedClient(Socket s, int p) {
        sock = s;
        port = p;
    }

    public void run() {
        try {
            // guard against inactive client
            sock.setSoTimeout(20000);
            // prepare record for logrecords DB
            StringBuffer record = new StringBuffer();
            InputStream in = sock.getInputStream();
            OutputStream out = sock.getOutputStream();
            byte[] buffer = new byte[512];
            in.read(buffer);
            String groupName = new String(buffer).trim();
            String timeStamp = new SimpleDateFormat("yy/MM/dd-HH:mm:ss").format(Calendar.getInstance().getTime());
            System.out.print(timeStamp + ": TCP request: " + groupName);
            byte[] responseBytes = null;

            // lookup key and encrypt message to client
            if (KeyStore.store.containsKey(groupName)) {
                SecretKey key = KeyStore.store.get(groupName);

                // get response from fortune command
                // String response = executeCommand("ps ax");
                String response = executeCommand("/usr/games/fortune -l -n 400");
                // String response = "A sheet of paper crossed my desk the other day and as I read it, realization of a basic truth came over me. So simple! So obvious we couldn't see it. John Knivlen, Chairman of Polamar Repeater Club, an amateur radio group, had discovered how IC circuits work. He says that smoke is the thing that makes ICs work because every time you let the smoke out of an IC circuit, it stops working. He claims to have verified this with thorough testing. I was flabbergasted! Of course! Smoke makes all things electrical work. Remember the last time smoke escaped from your Lucas voltage regulator Didn't it quit working? I sat and smiled like an idiot as more of the truth dawned. It's the wiring harness that carries the smoke from one device to another in your Mini, MG or Jag. And when the harness springs a leak, it lets the smoke out of everything at once, and then nothing works. The starter motor requires large quantities of smoke to operate properly, and that's why the wire going to it is so large. Feeling very smug, I continued to expand my hypothesis. Why are Lucas electronics more likely to leak than say Bosch? Hmmm... Aha!!! Lucas is British, and all things British leak! British convertible tops leak water, British engines leak oil, British displacer units leak hydrostatic fluid, and I might add Brititsh tires leak air, and the British defense unit leaks secrets... so naturally British electronics leak smoke. -- Jack Banton, PCC Automotive Electrical School [Ummm ... IC circuits? Integrated circuit circuits?]";                
                record.append(response);
                // ecnrypt response with RC4
                Cipher cipher = Cipher.getInstance("RC4");
                cipher.init(Cipher.ENCRYPT_MODE, key);
                responseBytes = cipher.doFinal(response.getBytes());
                System.out.println(", response: encrypted text sent, size: " + responseBytes.length);
            } else {
                String response = "Error: group name >" + groupName + "< not found in list of keys";
                responseBytes = response.getBytes();
                record.append( response);
                System.out.println(", response (" + responseBytes.length + "): " + response);
                groupName = "unknown";
            }
            // log what is sent
            DB.db.write(groupName, "TCP", sock.getInetAddress().toString(), port, record.toString());
            // send back to client
            out.write(responseBytes);
            sock.close();
        } catch (Exception e) {
            System.out.println("4. Exception caught: " + e);
            e.printStackTrace();
        }
        try {
            sock.close();
        } catch (Exception e) {
            System.out.println("5. Exception caught: " + e);
            e.printStackTrace();
        }
    }

    private String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return output.toString();
    }
}

class MonitorServerUDP extends Thread {

    public void run() {
        while (true) {
            try {
                Thread t = new PortServerUDP();
                t.start();
                t.join();
            } catch (InterruptedException e) {
                System.out.println("6. Exception caught: " + e);
                e.printStackTrace();
            }
        }
    }
}

class MonitorServerTCP extends Thread {

    public void run() {
        while (true) {
            try {
                Thread t = new PortServerTCP();
                t.start();
                t.join();
            } catch (InterruptedException e) {
                System.out.println("7. Exception caught: " + e);
                e.printStackTrace();
            }
        }
    }
}

public class PortServer {

    public static void main(String args[]) {
        new MonitorServerUDP().start();
        new MonitorServerTCP().start();
    }
}


