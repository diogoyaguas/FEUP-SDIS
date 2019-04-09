import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer implements RMI {

    private static double protocolVersion;
    private static int serverId;
    private static String peerAp;

    private static InetAddress MCAddress, MDBAddress, MDRAddress;
    private static int MCPort, MDBPort, MDRPort;

    private static ChannelBackup MDB;
    private static ChannelControl MC;
    private static ChannelRestore MDR;
    private static ScheduledThreadPoolExecutor exec;

    private static final AtomicInteger count = new AtomicInteger(0);
    private int peerID;

    private static MessageForwarder messageForwarder;


    private Peer() {
        peerID = count.incrementAndGet();

        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

        // communication channels initialization
        MC = new ChannelControl(MCAddress, MCPort);
        MDB = new ChannelBackup(MDBAddress, MDBPort);
        MDR = new ChannelRestore(MDRAddress, MDRPort);
    }

    static int getServerId() {
        return serverId;
    }

    static ChannelControl getMC() {
        return MC;
    }

    static ChannelBackup getMDB() {
        return MDB;
    }

    static ChannelRestore getMDR() {
        return MDR;
    }

    static MessageForwarder getMessageForwarder(){
        return messageForwarder;
    }

    public static void main(String[] args) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {

            if (!initializeArgs(args)) return;

            Peer obj = new Peer();

            RMI stub = (RMI) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(peerAp, stub);

            System.err.println("\nPeer " + peerAp + " ready");

        } catch (Exception e) {
            System.err.println("\nPeer exception: " + e.toString());
            e.printStackTrace();
        }

        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);

    }

    private static boolean initializeArgs(String[] args) throws UnknownHostException {

        if (args.length != 3 && args.length != 9) {
            System.out.println("Usage:\tPeer <protocolVersion> <serverId> <peerApp> <MCAddress> <MCPort> <MDBAddress> <MDBPort> <MDRAddress> <MDRPort>\nUsage:\tPeer <protocolVersion> <serverId> <peerApp>");
            return false;
        } else if (args.length == 3) { //User didn't specify so we use default values

            MCAddress = InetAddress.getByName("224.0.0.0");
            MCPort = 8000;

            MDBAddress = InetAddress.getByName("224.0.0.0");
            MDBPort = 8001;

            MDRAddress = InetAddress.getByName("224.0.0.0");
            MDRPort = 8002;

        } else { //User specify values

            MCAddress = InetAddress.getByName(args[3]);
            MCPort = Integer.parseInt(args[4]);

            MDBAddress = InetAddress.getByName(args[5]);
            MDBPort = Integer.parseInt(args[6]);

            MDRAddress = InetAddress.getByName(args[7]);
            MDRPort = Integer.parseInt(args[8]);
        }

        protocolVersion = Double.parseDouble(args[0]);
        serverId = Integer.parseInt(args[1]);
        peerAp = args[2];

        messageForwarder = new MessageForwarder(protocolVersion);

        printInfo();

        return true;
    }

    private static void printInfo() {

        System.out.println("\nVersion : " + protocolVersion + "\nServerId : " + serverId + "\nAccess Point : " + peerAp);
        System.out.println("\nMC: " + MCAddress + " | " + MCPort + "\nMDB: " + MDBAddress + " | " + MDBPort + "\nMDR: " + MDRAddress + " | " + MDRPort);
    }

    @Override
    public void backup(String filepath, int replicationDegree) throws RemoteException {

        try {

            File file = new File(filepath);
            byte[] file_data = FileData.loadFile(file);
            String final_file_id = FileData.getFileId(file);

            // gets number of chunks
            int chunks_num = file_data.length / (Chunk.getMaxSize()) + 1;

            for (int i = 0; i < chunks_num; i++) {

                // gets chunk data
                byte[] data;

                if (i == chunks_num - 1) {
                    if (file_data.length % Chunk.getMaxSize() == 0) {
                        data = new byte[0];
                    } else {
                        data = Arrays.copyOfRange(file_data, i * Chunk.getMaxSize(), i * Chunk.getMaxSize() + (file_data.length % Chunk.getMaxSize()));
                    }
                } else {
                    data = Arrays.copyOfRange(file_data, i * Chunk.getMaxSize(), (i + 1) * Chunk.getMaxSize());
                }

                // creates chunk
                Chunk chunk = new Chunk(i, final_file_id, data, replicationDegree);

                // chunk backup
                chunk.backup();

            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void restore(String filepath) throws RemoteException {

    }

    @Override
    public void delete(String filepath) throws RemoteException {

    }

    @Override
    public void reclaim(int size) throws RemoteException {

    }

    @Override
    public void state() throws RemoteException {

    }
}
