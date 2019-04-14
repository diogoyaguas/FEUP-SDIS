import java.io.File;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class App {

    /**
     * The host's remote name where the app will run
     */
    private static String peerAp;

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

            File file = FileData.createFolder("Files");

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
                    System.out.println("\nRestore finished\n");
                    break;

                case "DELETE":
                    stub.delete("TestFiles/" + filePath);
                    System.out.println("\nFile deleted\n");
                    break;

                case "RECLAIM":
                    int size = Integer.parseInt(args[2]);
                    stub.reclaim(size);
                    System.out.println("\nSpace reclaimed\n");
                    break;

                case "STATE":
                    stub.state();
                    break;

                default:
                    break;
            }

        } catch (RemoteException | NotBoundException e) {
            System.err.println("\nApp exception: " + e.toString());
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

        if (args.length != 2 && args.length != 3 && args.length != 4) {

            System.out.println("\n Usage:\tApp <peerAccessPoint> <subProtocol> \n");
            System.out.println(" Subprotocols :\t");
            System.out.println(" - BACKUP <fileID> <replicationDegree>\t");
            System.out.println(" - RESTORE <fileID>\t");
            System.out.println(" - DELETE <fileID>\t");
            System.out.println(" - RECLAIM <space>\t");
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