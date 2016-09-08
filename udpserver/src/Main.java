import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Main {

    public static void main(String[] args) throws Exception
    {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData;// = new byte[1024];
        while(true)
        {
            // receive
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            // print received info
            String sentence = new String( receivePacket.getData());
            System.out.println("RECEIVED: " + sentence);

            // get remote address
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();

            // do something to received info
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();

            // send it back to client
            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }
}
