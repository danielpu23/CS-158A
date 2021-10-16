import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    
	public static void main(String [] args) throws IOException {
	    DatagramSocket socket = new DatagramSocket(); 
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1000); // stream to send messages as object format
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter userName: ");
		String userName = sc.nextLine();
		
		MsgFormat m = new MsgFormat(0, userName, ""); // message format for joining a user
		oos.writeObject(m);
		byte[] data = baos.toByteArray();
		DatagramPacket buffer = new DatagramPacket(data, data.length, 
				InetAddress.getLocalHost(), 8989);
		socket.send(buffer);
		oos.close();
		baos.close();
		
		ReceiveThread t = new ReceiveThread(socket); // create and start thread to receive messages
		t.start();
		boolean keepSending = true;
		while(keepSending) {  // continues to ask for user input to send messages
			ByteArrayOutputStream baos2 = new ByteArrayOutputStream(1000);
			ObjectOutputStream oos2 = new ObjectOutputStream(baos2);
			System.out.println("Enter message or type \"!exitchat\" to leave: ");
			String message = sc.nextLine();
			int type;
			if(message.toLowerCase().contains("!exitchat")) { // client exits when they enter "exitchat"
			    type = 2;
			    keepSending = false;  // don't keep looping to send messages
			    MsgFormat m2 = new MsgFormat(type, userName, message);
                oos2.writeObject(m2);
                byte[] data2 = baos2.toByteArray();
                DatagramPacket buffer2 = new DatagramPacket(data2, data2.length, 
                        InetAddress.getLocalHost(), 8989);
                socket.send(buffer2);  // send message signaling an exit
                t.interrupt();         // since client is exiting, no longer need to receive messages
                oos2.close();
                baos2.close();  
			}
			else {               // if user doesn't want to exit, then send message normally
				type = 1;  
				MsgFormat m2 = new MsgFormat(type, userName, message);
				oos2.writeObject(m2);
				byte[] data2 = baos2.toByteArray();
				DatagramPacket buffer2 = new DatagramPacket(data2, data2.length, 
						InetAddress.getLocalHost(), 8989);
				socket.send(buffer2); // send message normally
				oos2.close();
				baos2.close();		
		   }
		}
		socket.close();
		sc.close();
	}
	
	// class that represents the format for a message, contains 3 parameters to
	// describe the message
	public static class MsgFormat implements Serializable {
		public int messageType;
		public String userName;
		public String messageContent;
		public MsgFormat(int messageType, String userName, String messageContent) {
			this.messageType = messageType;
			this.userName = userName;
			this.messageContent = messageContent;
		}
	}
	// threading class that's used to receive messages
	public static class ReceiveThread extends Thread{ 
	    public DatagramSocket socket;
	    public ReceiveThread(DatagramSocket socket) {
	        this.socket = socket;
	    }
	    public void run() {
            while(true) {
                try
                {            
                    Thread.sleep(500);             // put a slight delay on receiving messages 
                    DatagramPacket buffer = new DatagramPacket(new byte[1000], 1000); 
                    socket.receive(buffer);        // when a message is received, print the data received
                    byte[] m = buffer.getData();
                    System.out.println(new String(m));
                }
                catch ( InterruptedException | IOException e  ) // when thread is interrupted due to exit signal, close the socket and exit thread
                {                   
                    socket.close();
                    return;
                }
            }
        }
	}
}
