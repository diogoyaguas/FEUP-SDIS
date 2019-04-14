import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer implements RMI {

    /**
     * The protocol version being executed
     */
    private static double protocolVersion;

    /**
     * The server ID
     */
    private static int serverId;

    /**
     * The RMI access point for client communication
     */
    private static String peerAp;

    /**
     * Channels addresses
     */
    private static InetAddress MCAddress, MDBAddress, MDRAddress;

    /**
     * Channels ports
     */
    private static int MCPort, MDBPort, MDRPort;

    /**
     * Backup channel
     */
    private static ChannelBackup MDB;

    /**
     * Control channel
     */
    private static ChannelControl MC;

    /**
     * Restore channel
     */
    private static ChannelRestore MDR;

    private static ScheduledThreadPoolExecutor exec;

    /**
     * Storage
     */
    private static Storage storage;

    private static final AtomicInteger count = new AtomicInteger(0);

    /**
     * The peer's identifier
     */
    private static int peerID;

    /**
     * Peer Folder
     */
    private static File peerFolder;

    /**
     * Message Forwarder
     */
    private static MessageForwarder messageForwarder;

    /**
     * Constructor. Initiates peer from CLI args
     */
    private Peer() {
        peerID = count.incrementAndGet();

        peerFolder = FileData.createFolder("Files/" + peerAp);

        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

        // communication channels initialization
        MC = new ChannelControl(MCAddress, MCPort);
        MDB = new ChannelBackup(MDBAddress, MDBPort);
        MDR = new ChannelRestore(MDRAddress, MDRPort);
    }

    /**
     * Get Peer folder
     */
    static File getPeerFolder() {
        return peerFolder;
    }

    /**
     * Get server ID
     */
    static int getServerId() {
        return serverId;
    }

    /**
     * Get Peer ID
     */
    static int getPeerID() {
        return peerID;
    }

    /**
     * Get control channel
     */
    static ChannelControl getMC() {
        return MC;
    }

    /**
     * Get backup channel
     */
    static ChannelBackup getMDB() {
        return MDB;
    }

    /**
     * Get restore channel
     */
    static ChannelRestore getMDR() {
        return MDR;
    }

    /**
     * Get message forwarder
     */
    static MessageForwarder getMessageForwarder() {
        return messageForwarder;
    }

    /**
     * Get Peer storage
     */
    static Storage getStorage() {
        return storage;
    }

    public static void main(String[] args) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {

            if (!initializeArgs(args)) return;

            Peer obj = new Peer();

            RMI stub = (RMI) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry("localhost");
            registry.rebind(peerAp, stub);

            System.err.println("\nPeer " + peerAp + " ready");

        } catch (Exception e) {
            System.err.println("\nPeer exception: " + e.toString());
            e.printStackTrace();
        }

        storage = new Storage();

        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);

    }

    /**
     * Checks if peer arguments are correct
     *
     * @param args args to be checked
     * @return true if args are correct, false otherwise
     */
    private static boolean initializeArgs(String[] args) throws UnknownHostException {

        if (args.length != 3 && args.length != 9) {
            System.out.println("\n Usage:\tPeer <protocolVersion> <serverId> <peerApp> <MCAddress> <MCPort> <MDBAddress> <MDBPort> <MDRAddress> <MDRPort>\n      \tPeer <protocolVersion> <serverId> <peerApp>");
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

    /**
     * Print usage to show an user how to properly start a peer
     */
    private static void printInfo() {

        System.out.println("\nVersion - " + protocolVersion + "\nServerId - " + serverId + "\nAccess Point - " + peerAp);
        System.out.println("MC  " + MCAddress + ":" + MCPort + "\nMDB  " + MDBAddress + ":" + MDBPort + "\nMDR  " + MDRAddress + ":" + MDRPort);
    }

    /**
     * Backup file service.
     *
     * @param filepath          the file path
     * @param replicationDegree the desired replication degree
     */
    @Override
    public void backup(String filepath, int replicationDegree) {

        try {

            File file = new File(filepath);
            byte[] file_data = FileData.loadFile(file);
            String final_file_id = FileData.getFileId(file);

            // gets number of chunks
            assert file_data != null;
            int chunks_num = file_data.length / (Chunk.getMaxSize()) + 1;
            ArrayList<Chunk> storedChunks = new ArrayList<>();

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

                storedChunks.add(chunk);

                // chunk backup
                chunk.backup();

            }

            storage.addFile(final_file_id, file);
            storage.addDesiredReplicationDegree(final_file_id, replicationDegree);
            storage.addChunks(final_file_id, storedChunks);

            System.out.println("\nBackup finished");

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restore file service.
     *
     * @param filepath the file path
     */
    @Override
    public void restore(String filepath) {

        File file = new File(filepath);

        String FileId = "";

        try {
            FileId = FileData.getFileId(file);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        getMDR().startRestore(FileId);

        ArrayList<Chunk> chunks = new ArrayList<>();
        ArrayList<Chunk> finalChunks = new ArrayList<>();

        int i = 0;
        boolean lastChunk = false;
        long wait_time = 1;

        do {

            getMessageForwarder().sendGetChunk(i, FileId);

            try {
                TimeUnit.SECONDS.sleep(wait_time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (Peer.getMDR().getRestored(FileId).isEmpty()) {
                System.out.println("\nImpossible to restore this file");
                return;
            }

            Chunk chunk = Peer.getMDR().getRestored(FileId).get(i);

            //if chunk length is not 64 bytes , so it's the last byte
            if (chunk.getData().length != Chunk.getMaxSize())
                lastChunk = true;


            chunks.add(chunk);
            i++;

        } while (!lastChunk);

        for (Chunk storedChunk : chunks) {
            if (!finalChunks.contains(storedChunk))
                finalChunks.add(storedChunk);
        }

        getMDR().stopRestore(FileId);

        byte[] dataBody = new byte[0];
        byte[] tmp = new byte[0];

        for (Chunk chunk : finalChunks) {

            byte[] chunkBody = chunk.getData();

            tmp = new byte[dataBody.length + chunkBody.length];
            System.arraycopy(dataBody, 0, tmp, 0, dataBody.length);
            System.arraycopy(chunkBody, 0, tmp, dataBody.length, chunkBody.length);

            dataBody = tmp;

        }

        System.out.println(finalChunks.size());

        File restoreFolder = FileData.createFolder("Files/" + Peer.getPeerFolder().getName() + "/restore");
        FileOutputStream restore;

        try {

            restore = new FileOutputStream(restoreFolder.getPath() + "/" + file.getName());
            restore.write(dataBody);
            restore.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nRestore finished");
    }

    /**
     * Delete file service.
     *
     * @param filepath the file path
     */
    @Override
    public void delete(String filepath) {

        File file = new File(filepath);
        String file_id = "";

        try {
            file_id = FileData.getFileId(file);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        storage.deleteFile(file_id);
        storage.deleteDesiredReplicationDegree(file_id);
        storage.deleteFileChunks(file_id);

        Peer.getMessageForwarder().sendDelete(file_id);

        System.out.println("\nFile deleted");

    }

    /**
     * Reclaim space service.
     *
     * @param size new value for reserved peer storage space
     */
    @Override
    public void reclaim(int size) {

        long spaceUsed = Peer.getStorage().getOccupiedSpace();
        long spaceClaimed = size * 1000;

        if (spaceUsed <= spaceClaimed) {
            storage.reclaimSpace(spaceClaimed, spaceUsed);
            return;
        }

        ArrayList<Chunk> chunks = Peer.getStorage().getStoredChunks();

        long tempSpace = spaceUsed - spaceClaimed;

        System.out.println(tempSpace);

        Iterator iter = chunks.iterator();

        do {
            Chunk chunk = (Chunk) iter.next();

            Peer.getMessageForwarder().sendRemoved(chunk.getChunkNr(), chunk.getFileID());

            tempSpace -= chunk.getData().length;

            iter.remove();

            String fileName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + chunk.getFileID() + "/chk" + chunk.getChunkNr();
            File file = new File(fileName);
            file.delete();

            String folderName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + chunk.getFileID();
            File folder = new File(folderName);
            folder.delete();

        } while (tempSpace > 0);

        long totalSpaceOccupied = 0;

        for (Chunk chunk : chunks) {
            totalSpaceOccupied += chunk.getData().length;
        }

        Peer.getStorage().reclaimSpace(spaceClaimed, totalSpaceOccupied);

    }

    /**
     * Retrieve state service.
     */
    @Override
    public void state() {

        System.out.println("\n| Peer State |");

        //Each file whose backup it has initiated
        int i = 1;

        for (String key : storage.getFiles().keySet()) {

            System.out.println("\n| File " + i + " |");

            //File pathname
            System.out.println("\tFile Pathname - " + storage.getFiles().get(key).getPath() + "\t");

            //Backup service id of the file
            System.out.println("\tFile Id - " + key);

            //Desired replication degree
            System.out.println("\tDesired replication degree - " + storage.getDesiredReplicationDegree().get(key));

            ArrayList<Chunk> chunks = storage.getFileChunks().get(key);

            for (Chunk chunk : chunks) {

                System.out.println("\n\tChunk " + (chunk.getChunkNr() + 1));

                System.out.println("\t\tChunk ID - " + chunk.getID());
                System.out.println("\t\tChunk perceived replication degree - " + chunk.getRepDegree());

            }

            i++;
        }

        //Each chunk it stores
        if (!storage.getStoredChunks().isEmpty()) {
            System.out.println("\n| Stored Chunks |");
            i = 1;
        }

        for (Chunk chunk : storage.getStoredChunks()) {

            System.out.println("\n\tChunk " + i);
            //ID
            System.out.println("\t\tChunk ID - " + chunk.getID() + "\t");

            //Size (in KB)
            System.out.println("\t\tChunk Size - " + (chunk.getData().length / 1000) + " KB \t");

            //Perceived replication degree
            System.out.println("\t\tChunk Replication Degree - " + chunk.getRepDegree() + "\t");

            i++;
        }

        //Peer's storage capacity
        System.out.println("\n| Storage Capacity |\n");

        //Maximum amount of disk space to store chunks
        System.out.println("\tMaximum amount of Disk Space to store chunks - " + storage.getSpaceAvailable() / 1000 + " KB \t");

        //Amount of storage to backup chunks
        System.out.println("\tAmount of storage used to backup the chunks - " + storage.getOccupiedSpace() / 1000 + " KB \t");

    }
}
