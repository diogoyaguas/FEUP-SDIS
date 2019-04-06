package Messages;

import java.net.DatagramPacket;

public class MessageHandler implements Runnable {

    DatagramPacket packet;

    String[] header;

    public MessageHandler(DatagramPacket packet){
        this.packet = packet;
    }

    @Override
    public void run() {

    }
}
