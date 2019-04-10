import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;

public class App {

    private static String peerAp, subProtocol, filePath;

    private App() {
    }

    public static void main(String[] args) throws IOException {

        try {

            if (!initializeArgs(args)) return;

            Registry registry = LocateRegistry.getRegistry("localhost");
            RMI stub = (RMI) registry.lookup(peerAp);

            switch (subProtocol) {

                case "BACKUP":
                    int repDegree = Integer.parseInt(args[3]);
                    stub.backup("TestFiles/" + filePath, repDegree);
                    System.out.println("\nBackup finished\n");
                    break;

                case "RESTORE":
                    stub.restore("TestFiles/" + filePath);
                    break;

                case "DELETE":
                    stub.delete("TestFiles/" + filePath);
                    System.out.println("\n" + filePath + "deleted\n");
                    break;

                case "RECLAIM":
                    int size = Integer.parseInt(args[3]);
                    stub.reclaim(size);
                    break;

                case "STATE":
                    stub.state();
                    break;

                default:
                    break;
            }

        } catch (Exception e) {
            System.err.println("\nApp exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static boolean initializeArgs(String[] args) throws RemoteException {

        if (args.length != 2 && args.length != 3 && args.length != 4) {

            System.out.println("\n Usage:\tApp <peerAccessPoint> <subProtocol> \n");
            System.out.println(" Subprotocols :\t");
            System.out.println(" - BACKUP <fileID> <replicationDegree>\t");
            System.out.println(" - RESTORE <fileID>\t");
            System.out.println(" - DELETE <fileID>\t");
            System.out.println(" - RECLAIM <fileID> <space>\t");
            System.out.println(" - STATE\n");
            return false;
        }

        peerAp = args[0];
        subProtocol = args[1];

        if (args.length > 2)
            filePath = args[2];

        return true;
    }

}