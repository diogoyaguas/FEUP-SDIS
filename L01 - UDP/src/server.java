import java.io.*;
import java.net.*;
import java.util.*;

class server {

    private static int port;
    private static Hashtable<String, String> cars;

    public static void main(String args[]) throws IOException {

        if (args.length != 1) {

            System.out.println("Usage:\tjava Server <port>");
            return;

        } else {

            port = Integer.parseInt(args[0]);
        }

        cars = new Hashtable<String, String>();

        DatagramSocket serverSocket = new DatagramSocket(port);

        boolean done = false;
        while (!done) {

            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            serverSocket.receive(packet);
            String request = new String(packet.getData(), 0, packet.getLength());
            System.out.println("\nRequest: " + request);

            char oper = request.charAt(0);

            String response = "";
            String plate = "";
            String owner = "";

            switch (oper) {
            case 'L':
                plate = request.substring(1);
                if (cars.containsKey(plate)) {
                    response = cars.get(plate);
                } else {
                    response = "NOT_FOUND";
                }

                break;

            case 'R':

                String plateOwner = request.substring(1);
                String[] parts = plateOwner.split(";");
                plate = parts[0];
                owner = parts[1];
                if (cars.containsKey(plate)) {
                    response = "-1";
                } else {
                    cars.put(plate, owner);
                    response = Integer.toString(cars.size());
                }

                break;

            default:
                break;
            }

            System.out.println("Sent: " + response);
            data = response.getBytes();
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(data, data.length, address, port);
            serverSocket.send(packet);
        }

        serverSocket.close();
    }
}