import java.io.*;
import java.net.*;
import java.util.*;

class server extends TimerTask {

    private static int srvc_port;
    private static String srvc_addr = "localhost";
    private static InetAddress mcast_addr;
    private static int mcast_port;
    private static MulticastSocket mSocket;
    private static Hashtable<String, String> cars;

    @Override
    public void run() {

        String msg = srvc_addr + ";" + srvc_port;

        DatagramPacket mcast_packet = new DatagramPacket(msg.getBytes(), msg.getBytes().length, mcast_addr, mcast_port);

        try {
            mSocket.send(mcast_packet);

        } catch (IOException e) {

            e.printStackTrace();
        }

        System.out.println("multicast: " + mcast_addr + " " + mcast_port + ": " + srvc_addr + " " + srvc_port + "\n");
    }

    public static void main(String args[]) throws IOException {

        if (args.length != 3) {

            System.out.println("Usage:\tjava Server <srvc_port> <mcast_addr> <mcast_port>");
            return;

        }

        else if (!utils.isValidRange("224.0.0.0", "239.255.255.255", args[1])) {

            System.out.println("IP Address Error: Must be in the range 224.0.0.0 to 239.255.255.255\n");
            return;
        } else {
            srvc_port = Integer.parseInt(args[0]);
            mcast_addr = InetAddress.getByName(args[1]);
            mcast_port = Integer.parseInt(args[2]);
        }

        cars = new Hashtable<String, String>();

        mSocket = new MulticastSocket(mcast_port);
        mSocket.setTimeToLive(1);

        DatagramSocket serverSocket = new DatagramSocket(srvc_port);

        TimerTask timerTask = new server();

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 1000);

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
            int srvc_port = packet.getPort();
            packet = new DatagramPacket(data, data.length, address, srvc_port);
            serverSocket.send(packet);
        }

        serverSocket.close();
    }
}