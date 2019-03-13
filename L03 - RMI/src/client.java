import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class client {

    private static String hostName;
    private static String rmObjectName;
    private static String oper, plate, owner;

    public static void main(String args[]) throws IOException {

        if (args.length < 4 || args.length > 5) {

            System.out.println("Usage:\tjava Client <host_name> <remote_object_name> <oper> <opnd>");
            return;

        } else {

            hostName = args[0];
            rmObjectName = args[1];
            oper = args[2];

            if (oper.equals("register")) {

                plate = args[3];
                owner = args[4];

            } else if (oper.equals("lookup")) {

                plate = args[3];
            } else
                System.out.println("Invalid Operation");
        }

        try {

            Registry registry = LocateRegistry.getRegistry(hostName);
            remoteInterface stub = (remoteInterface) registry.lookup(rmObjectName);

            switch (oper) {
            case "lookup":
                String lookupReq = stub.lookup(plate);
                System.out.println("lookup " + plate + " :: " + lookupReq);
                break;

            case "register":
                int registerReq = stub.register(plate, owner);
                System.out.println("register" + plate + " :: " + registerReq);
                break;
            }
        }

        catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }

    }
}