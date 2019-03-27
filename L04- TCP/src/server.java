import java.io.*;
import java.net.*;
import java.util.*;

class server {

    private static int port;
    private static Hashtable<String, String> cars;
    private static PrintWriter out = null;
    private static BufferedReader in = null;

    public static void main(String args[]) throws IOException {

        if (args.length != 1) {

            System.out.println("Usage:\tjava Server <port>");
            return;

        } else {

            port = Integer.parseInt(args[0]);
        }

        cars = new Hashtable<String, String>();

        ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("Impossible listen on port: " + port);
			System.exit(-1);
		}

        boolean done = false;
        while (!done) {

            Socket socket = null;

			try {
				socket = serverSocket.accept();
			} catch (IOException e) {
				System.err.println("Accept failed: " + port);
				System.exit(1);
            }
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

            String request = in.readLine();
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

            out.println(response);

			System.out.println("Sent: " + response);

			out.close();
			in.close();

			socket.close();
        }

        serverSocket.close();
    }
}