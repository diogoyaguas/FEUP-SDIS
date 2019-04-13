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
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    private static Storage storage;

    private static final AtomicInteger count = new AtomicInteger(0);
    private static int peerID;
    private static File peerFolder;

    private static MessageForwarder messageForwarder;


    private Peer() {
        peerID = count.incrementAndGet();

        peerFolder = FileData.createFolder("Files/" + peerAp);

        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);

        // communication channels initialization
        MC = new ChannelControl(MCAddress, MCPort);
        MDB = new ChannelBackup(MDBAddress, MDBPort);
        MDR = new ChannelRestore(MDRAddress, MDRPort);
    }

    //GETS
    static File getPeerFolder() {
        return peerFolder;
    }

    static int getServerId() {
        return serverId;
    }

    static int getPeerID() {
        return peerID;
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

    static MessageForwarder getMessageForwarder() {
        return messageForwarder;
    }

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

    private static void printInfo() {

        System.out.println("\n Version - " + protocolVersion + " | ServerId - " + serverId + " | Access Point - " + peerAp);
        System.out.println(" MC  " + MCAddress + ":" + MCPort + " | MDB  " + MDBAddress + ":" + MDBPort + " | MDR  " + MDRAddress + ":" + MDRPort);
    }

    @Override
    public void backup(String filepath, int replicationDegree) {

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

            System.out.println("\nBackup finished");

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

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

        getMDR().stopRestore(FileId);

        byte[] dataBody = new byte[0];
        byte[] tmp;

        for (Chunk chunk : chunks) {

            byte[] chunkBody = chunk.getData();

            tmp = new byte[dataBody.length + chunkBody.length];
            System.arraycopy(dataBody, 0, tmp, 0, dataBody.length);
            System.arraycopy(chunkBody, 0, tmp, dataBody.length, chunkBody.length);

            dataBody = tmp;
        }

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

    @Override
    public void delete(String filepath) {

        File file = new File(filepath);
        String file_id = "";

        try {
            file_id = FileData.getFileId(file);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        Peer.getMessageForwarder().sendDelete(file_id);

        System.out.println("\nFile deleted");

    }

    @Override
    public void reclaim(int size) {

        long spaceUsed = Peer.getStorage().getSpaceUsed();
        long spaceClaimed = size;

        System.out.println("1.Usado: " + spaceUsed);
        System.out.println("1.Livre: " + Peer.getStorage().getSpaceAvailable());
        System.out.println("1.Preciso: " + spaceClaimed);
        System.out.println("");

        if (spaceUsed == 0) {
            System.out.println("No space used. Impossible to reclaim space");
            return;
        } else if (spaceUsed < spaceClaimed) {
            spaceClaimed = spaceUsed;
        }

        System.out.println("2.Usado: " + spaceUsed);
        System.out.println("2.Livre: " + Peer.getStorage().getSpaceAvailable());
        System.out.println("2.Preciso: " + spaceClaimed);
        System.out.println("");

        HashSet<Chunk> chunks = Peer.getStorage().getStoredChunks();

        int i = 0;
        long tempSpace = spaceClaimed;

        Iterator iter = chunks.iterator();

        do {
            Chunk chunk = (Chunk) iter.next();

            Peer.getMessageForwarder().sendRemoved(chunk.getChunkNr(), chunk.getFileID());

            iter.remove();

            tempSpace -= chunk.getData().length;

            String fileName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + chunk.getFileID() + "/chk" + chunk.getChunkNr();
            File file = new File(fileName);
            file.delete();

            String folderName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + chunk.getFileID();
            File folder = new File(folderName);
            folder.delete();

            i++;

        } while (tempSpace > 0 && i < chunks.size());

        Peer.getStorage().reclaimSpace(spaceClaimed);

        System.out.println("3.Usado: " + Peer.getStorage().getSpaceUsed());
        System.out.println("3.Livre: " + Peer.getStorage().getSpaceAvailable());
        System.out.println("3.Preciso: " + spaceClaimed);
        System.out.println("\nSpace reclaimed");
    }

    @Override
    public void state() {

        //Each file whose backup it has initiated

        for(int i = 0; i < storage.getFiles().size(); i++){

            //the file pathname
            System.out.println("File Pathname - " + storage.getFiles().get(i).getFile().getAbsolutePath() + "\t");

            //backup service id of the file
            try {
                String FileId = FileData.getFileId(storage.getFiles().get(i).getFile());
                System.out.println("File Id  - " + FileId + "\t");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }


            // TO DO
            //desired replication degree
           // System.out.println("Desired Replication Degree - "  storage.getFiles().get(i).get);

            //Each chunk of the file


            //id
            //perceived replication degree
        }

        //Each chunk it stores
    for(int k = 0; k < storage.getStoredChunks().size(); k++){

        //id
        System.out.println("Chunk ID - " + storage.getStoredChunks().get(k).getID() + "\t");

        //size (in KB)
        System.out.println("Chunk Size - " + (storage.getStoredChunks().get(k).getData()).length + " KB \t");

        //perceived replication degree
        String key = storage.getStoredChunks().get(k).getFileID() + '_' + storage.getStoredChunks().get(k).getChunkNr();
        System.out.println("Chunk Replication Degree - " + storage.getStoredReps().get(key) + "\t");
    }

        //peer's storage capacity

        //maximum amount of disk space to store chunks
        System.out.println("Maximum amount of Disk Space to store chunks - " + storage.getSpaceAvailable()/1000 + " KB \t");

        //amount of storage to backup chunks
        System.out.println("Maximum amount of Disk Space to store chunks - " + storage.getOccupiedSpace()/1000 + " KB \t");

    }
}
