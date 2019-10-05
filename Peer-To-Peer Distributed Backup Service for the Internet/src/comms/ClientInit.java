package comms;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClientInit {

    /**
     * The host's remote name where the app will run
     */
    private static String peerId;

    /**
     * The subprotocol that the app will run
     */
    private static String subProtocol;

    /**
     * The file path that the app will run
     */
    private static String filePath;

    public static void main(String[] args) {

        try {

            if (!initializeArgs(args))
                return;

            RMI stub = null;

            try {

                Registry registry = LocateRegistry.getRegistry("localhost");
                stub = (RMI) registry.lookup(peerId);

            } catch (RemoteException error) {
                error.printStackTrace();
            }

            switch (subProtocol) {

            case "BACKUP":
                int repDegree = Integer.parseInt(args[3]);
                stub.backup("../TestFiles/" + filePath, repDegree);
                System.out.println("\nBackup finished\n");
                break;

            case "RESTORE":
                stub.restore(filePath);
                System.out.println("\nRestore finished\n");
                break;

            case "DELETE":
                stub.delete("../TestFiles/" + filePath);
                System.out.println("\nFile deleted\n");
                break;

            case "RECLAIM":
                int size = Integer.parseInt(args[2]);
                stub.reclaim(size);
                System.out.println("\nSpace reclaimed\n");
                break;
            default:
                break;
            }

        } catch (RemoteException | NotBoundException e) {
            System.err.println("\nClientInit exception: " + e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Checks if peer arguments are correct
     *
     * @param args args to be checked
     * @return true if args are correct, false otherwise
     */
    private static boolean initializeArgs(String[] args) {

        if (args.length != 3 && args.length != 4) {

            System.out.println("\n Usage:\tClientInit <peerAccessPoint> <subProtocol> \n");
            System.out.println(" Subprotocols :\t");
            System.out.println(" - BACKUP <fileID> <replicationDegree>\t");
            System.out.println(" - RESTORE <fileID>\t");
            System.out.println(" - DELETE <fileID>\t");
            System.out.println(" - RECLAIM <space>\t");
            return false;
        }

        peerId = args[0];
        subProtocol = args[1];
        filePath = args[2];

        return true;
    }
}