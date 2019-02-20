import java.io.*;
import java.net.*;

public class client {

    private static String hostName;
    private static int port;
    private static String oper;
    private static String plate, owner;

    public static void main(String args[]) throws IOException {

        if (args.length < 4 || args.length > 5) {

            System.out.println("Usage:\tjava Client <host_name> <port_number> <oper> <opnd>");
            return;

        } else {

            hostName = args[0];
            port = Integer.parseInt(args[1]);
            oper = args[2];

            if (oper.equals("register")) {

                plate = args[3];
                owner = args[4];
            } else if (oper.equals("lookup")) {

                plate = args[3];
            } else
                System.out.println("Invalid Operation");
        }

        String request = "";

        switch (oper) {

        case "register":

            request += 'R' + plate + ";" + owner;
            break;

        case "lookup":

            request += 'L' + plate;
            break;
        }

        DatagramSocket clientSocket = new DatagramSocket();

        byte[] data = request.getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
        clientSocket.send(packet);
        System.out.println("Sent: " + request);

        packet = new DatagramPacket(data, data.length);
        clientSocket.receive(packet);
        String response = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Receveid: " + response);

        clientSocket.close();
    }
}