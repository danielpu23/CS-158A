import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class Server {
	public static void main(String [] args) throws IOException, ClassNotFoundException {
		DatagramSocket socket = new DatagramSocket(8989);
		DatagramPacket buffer = new DatagramPacket(new byte[1000], 1000);
		
		Set<Integer> ports  = new HashSet<>(); // list of all client ports
		boolean keepReceiving = true;
		while(keepReceiving) {                // continuously check if messages are received
			socket.receive(buffer);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.getData()));
			Client.MsgFormat m = (Client.MsgFormat) ois.readObject(); // get the streamed message object from client
			if(m.messageType == 0) {                                  // if user joined, print the username and information
				System.out.println("User " + m.userName + " has logged in to port " + buffer.getPort()+ " from the IP address " + buffer.getAddress());			
				ports.add(buffer.getPort());
			}
			else if(m.messageType == 1) {           // if user sends normal message
			    String userAndMessage = m.userName+ ": " +  m.messageContent;
				System.out.println(userAndMessage); // print the user and its message
				for(int port: ports) {              // go through all the stored ports and send to all ports except the one that just sent it
				    if(port != buffer.getPort()) {
				       byte [] msg = userAndMessage.getBytes();
				       DatagramPacket buffer2 = new DatagramPacket(msg, msg.length, 
			                InetAddress.getLocalHost(), port);
				       socket.send(buffer2);
				    }
				}
			}
			else {                                   // if user wants to exit (messageType == 2), remove their port from all stored ports and print their information
				System.out.println(m.userName + " has left the chat from port " + 
			buffer.getPort() + " at the IP address " + buffer.getAddress());
				ports.remove(buffer.getPort());
			}
			if(ports.isEmpty()) {                    // if nothing in ports, meaning no more users, stop receiving messages
			    keepReceiving = false;
			}
			ois.close();
		}	
		socket.close();
	}
}