import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Peer storage, where the peer's state is kept
 */
class Storage implements java.io.Serializable {

    /**
     * Peer's files that are backup
     */
    private HashMap<String, File> files;
    private HashMap<String, Integer> desiredReplicationDegree;
    private HashMap<String, ArrayList<Chunk>> fileChunks;

    private ArrayList<Chunk> storedChunks;
    private ConcurrentHashMap<String, Integer> storedReps;

    private long MAX_SPACE = 1000000000;

    /**
     * Currently used space for backing chunks
     */
    private long spaceAvailable;

    /**
     * Currently used space for backing chunks
     */
    private long spaceUsed;

    /**
     * Peer total space
     */
    private long totalSpace;

    /**
     * Initiates the file system manager
     */
    Storage() {

        this.files = new HashMap<>();
        this.desiredReplicationDegree = new HashMap<>();
        this.fileChunks = new HashMap<>();

        this.storedChunks = new ArrayList<>();
        this.storedReps = new ConcurrentHashMap<>();

        this.spaceAvailable = MAX_SPACE;
        this.spaceUsed = 0;
        this.totalSpace = spaceAvailable + spaceUsed;
    }


    HashMap<String, File> getFiles() {

        return files;
    }

    File getFile(String fileID) {

        return  files.get(fileID);
    }

    HashMap<String, Integer> getDesiredReplicationDegree() {

        return desiredReplicationDegree;
    }

    HashMap<String, ArrayList<Chunk>> getFileChunks() {

        return fileChunks;
    }

    ArrayList<Chunk> getStoredChunks() {

        return storedChunks;
    }

    long getSpaceAvailable() {

        return spaceAvailable;
    }

    long getOccupiedSpace() {

        spaceUsed = totalSpace - spaceAvailable;
        return spaceUsed;
    }

    /**
     * Saves a file locally
     *
     * @param fileID the file ID to save the file
     * @param file   the file
     */
    void addFile(String fileID, File file) {

        files.put(fileID, file);
    }

    /**
     * Saves a desired replication degree
     *
     * @param fileID            the file ID to save the file
     * @param replicationDegree the replication degree
     */
    void addDesiredReplicationDegree(String fileID, int replicationDegree) {

        desiredReplicationDegree.put(fileID, replicationDegree);
    }

    /**
     * Saves chunks
     *
     * @param fileID        the file ID to save the file
     * @param storedChuncks the chunks
     */
    void addChunks(String fileID, ArrayList<Chunk> storedChuncks) {

        fileChunks.put(fileID, storedChuncks);
    }

    /**
     * Tries to store a chunk
     *
     * @param chunk the chunk to store
     */
    synchronized void addStoredChunk(Chunk chunk) {

        byte[] chunk_data = chunk.getData();

        // check if there is enough memory
        long tempSpace = spaceAvailable - chunk_data.length;

        if (tempSpace < 0) {
            System.out.println("\nChunk can not be stored.");
            System.out.println("Not enough free memory.");
            return;
        }

        File backupFolder = FileData.createFolder("Files/" + Peer.getPeerFolder().getName() + "/backup");
        File fileFolder = FileData.createFolder("Files/" + Peer.getPeerFolder().getName() + "/backup/" + chunk.getFileID());

        FileOutputStream out;
        try {

            out = new FileOutputStream(fileFolder.getAbsolutePath() + "/chk" + chunk.getChunkNr());
            out.write(chunk.getData());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (!isStoredAlready(chunk)) {

            // update memory status
            decreaseSpace(chunk_data.length);

            this.storedChunks.add(chunk);
            String key = chunk.getFileID() + '_' + chunk.getChunkNr();
            this.storedReps.put(key, chunk.getRepDegree());
        }
    }

    /**
     * Verify if chunks is already stored
     *
     * @param chunk the chunk to store
     */
    boolean isStoredAlready(Chunk chunk) {

        for (Chunk stored : this.storedChunks) {
            if (stored.getFileID().equals(chunk.getFileID()) && stored.getChunkNr() == chunk.getChunkNr())
                return true;
        }

        return false;
    }

    /**
     * Delete file
     *
     * @param fileID File ID
     */
    void deleteFile(String fileID) {

        files.remove(fileID);
    }

    /**
     * Delete desired replication degree
     *
     * @param fileID File ID
     */
    void deleteDesiredReplicationDegree(String fileID) {

        desiredReplicationDegree.remove(fileID);
    }

    /**
     * Delete all file chunks
     *
     * @param fileID File ID
     */
    void deleteFileChunks(String fileID) {

        fileChunks.remove(fileID);
    }

    /**
     * Delete stored chunks
     *
     * @param fileID File ID
     */
    void deleteStoredChunk(String fileID) {

        for (Iterator<Chunk> it = this.storedChunks.iterator(); it.hasNext(); ) {

            Chunk stored = it.next();

            if (stored.getFileID().equals(fileID)) {
                String fileName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + fileID + "/chk" + stored.getChunkNr();
                File file = new File(fileName);
                file.delete();

                //removeReps
                deleteReps(fileID, stored.getChunkNr());

                //freeSpace
                freeSpace(fileID, stored.getChunkNr());

                it.remove();
            }
        }

        String folderName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + fileID;
        File folder = new File(folderName);
        folder.delete();

    }

    /**
     * Delete stored replications degrees
     *
     * @param fileID File ID
     */
    private synchronized void deleteReps(String fileID, int ChunkNr) {

        this.storedReps.remove(fileID + '_' + ChunkNr);
    }

    /**
     * Delete stored chunk files
     *
     * @param chunk Chunk
     */
    void deleteChunkFiles(Chunk chunk) {
        String fileName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + chunk.getFileID() + "/chk" + chunk.getChunkNr();
        File file = new File(fileName);
        file.delete();

        String folderName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + chunk.getFileID();
        File folder = new File(folderName);
        folder.delete();
    }

    /**
     * Reclaim space
     *
     * @param spaceClaimed space to be claimed
     * @param spaceUsed    space used
     */
    void reclaimSpace(long spaceClaimed, long spaceUsed) {

        this.totalSpace = spaceClaimed;
        this.spaceAvailable = spaceClaimed - spaceUsed;
        this.spaceUsed = spaceUsed;
    }

    /**
     * Decrease available space
     *
     * @param chunkSize Size of chunk
     */
    private synchronized void decreaseSpace(int chunkSize) {

        spaceAvailable -= chunkSize;
    }

    /**
     * Free space
     *
     * @param fileID  File ID
     * @param ChunkNr Chunk Number
     */
    private synchronized void freeSpace(String fileID, int ChunkNr) {

        for (Chunk stored : this.storedChunks) {
            if (stored.getFileID().equals(fileID) && stored.getChunkNr() == ChunkNr)
                spaceAvailable += stored.getData().length;
        }
    }

    /**
     * Decrease replication degree
     *
     * @param fileID  File ID
     * @param ChunkNr Chunk Number
     */
    synchronized void decreaseRepDegree(String fileID, int ChunkNr) {

        String key = fileID + '_' + ChunkNr;
        int total = this.storedReps.get(key) - 1;
        this.storedReps.replace(key, total);
    }

    /**
     * Increase replication degree
     *
     * @param fileID  File ID
     * @param ChunkNr Chunk Number
     */
    synchronized void increaseRepDegree(String fileID, int ChunkNr) {

        String key = fileID + '_' + ChunkNr;
        int total = this.storedReps.get(key) + 1;
        this.storedReps.replace(key, total);
    }

}