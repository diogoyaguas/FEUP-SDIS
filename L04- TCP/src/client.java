import java.io.*;
import java.net.*;

public class client {

  private static String hostName;
  private static int port;
  private static String oper;
  private static String plate, owner;
  private static PrintWriter out = null;
  private static BufferedReader in = null;

  public static void main(String args[]) throws IOException {

    if (args.length < 4 || args.length > 5) {

      System.out.println(
          "Usage:\tjava Client <host_name> <port_number> <oper> <opnd>");
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

      request = 'R' + plate + ";" + owner;
      break;

    case "lookup":

      request = 'L' + plate;
      break;
    }

    Socket clientSocket = new Socket(hostName, port);

    out = new PrintWriter(clientSocket.getOutputStream(), true);
    in = new BufferedReader(
        new InputStreamReader(clientSocket.getInputStream()));

    out.println(request);
    System.out.println("\nSent: " + request);

    String response = in.readLine();
    System.out.println("Receveid: " + response +"\n");

    out.close();
    in.close();
    clientSocket.close();
  }
}