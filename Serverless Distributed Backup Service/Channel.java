import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public abstract class Channel implements Runnable {

    private static final int PACKET_MAX_SIZE = 2048;
    private MulticastSocket socket;

    private InetAddress address;
    private int port;

    Channel(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public void run() {
        openSocket();

        byte[] buf = new byte[PACKET_MAX_SIZE];
        boolean done = false;

        // receive messages
        while (!done) {
            try {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                new Thread(new MessageHandler(packet)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        closeSocket();
    }

    private void openSocket() {
        try {
            socket = new MulticastSocket(port);

            socket.setTimeToLive(1);

            socket.joinGroup(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSocket() {
        if (socket != null)
            socket.close();
    }

    synchronized void sendMessage(byte[] msg) { // only one thread can access the resource at a given point of time
        DatagramPacket packet = new DatagramPacket(msg, msg.length, address, port);

        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //save?

}
