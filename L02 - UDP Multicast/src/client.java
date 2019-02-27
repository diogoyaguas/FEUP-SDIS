import java.io.*;
import java.net.*;

public class client {

    private static int srvc_port;
    private static InetAddress mcast_addr;
    private static InetAddress srvc_addr;
    private static int mcast_port;
    private static String oper;
    private static String plate, owner;

    public static void main(String args[]) throws IOException {

        if (args.length < 4 || args.length > 5) {
            System.out.println("Usage:\tjava Client <mcast_addr> <mcast_port> <oper> <opnd>");
            return;
        }

        else if (!utils.isValidRange("224.0.0.0", "239.255.255.255", args[0])) {
            System.out.println("IP Address Error: Must be in the range 224.0.0.0 to 239.255.255.255\n");
            return;
        }

        else {

            mcast_addr = InetAddress.getByName(args[0]);
            mcast_port = Integer.parseInt(args[1]);
            oper = args[2];

            if (oper.equals("register")) {

                plate = args[3];
                owner = args[4];
            } else if (oper.equals("lookup")) {

                plate = args[3];
            } else
                System.out.println("Invalid Operation");
        }

        MulticastSocket mcast_socket = new MulticastSocket(mcast_port);
        mcast_socket.joinGroup(mcast_addr);

        byte[] data = new byte[1024];
        DatagramPacket mcast_packet = new DatagramPacket(data, data.length);
        mcast_socket.receive(mcast_packet);
        String msg = new String(data, 0, data.length).trim();

        String[] parts = msg.split(";");
        srvc_addr = InetAddress.getByName(parts[0]);
        srvc_port = Integer.parseInt(parts[1]);

        System.out.println("multicast: " + mcast_addr + " " + mcast_port + ": " + srvc_addr + " " + srvc_port + "\n");

        String request = "";

        switch (oper) {

        case "register":

            request = 'R' + plate + ";" + owner;
            break;

        case "lookup":

            request = 'L' + plate;
            break;
        }

        DatagramSocket clientSocket = new DatagramSocket();

        data = request.getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, srvc_addr, srvc_port);
        clientSocket.send(packet);
        clientSocket.setSoTimeout(2000);
        System.out.println("\nSent: " + request);

        packet = new DatagramPacket(data, data.length);
        clientSocket.receive(packet);
        String response = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Received: " + response);

        clientSocket.close();
    }
}