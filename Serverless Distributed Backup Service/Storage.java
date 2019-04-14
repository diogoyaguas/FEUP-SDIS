import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

class Storage implements java.io.Serializable {

    private HashMap<String, File> files;
    private HashMap<String, Integer> desiredReplicationDegree;
    private HashMap<String, ArrayList<Chunk>> fileChunks;

    private HashSet<Chunk> storedChunks;
    private ConcurrentHashMap<String, Integer> storedReps;

    private long MAX_SPACE = 1000000000;
    private long spaceAvailable;
    private long spaceUsed;

    Storage() {

        this.files = new HashMap<>();
        this.desiredReplicationDegree = new HashMap<>();
        this.fileChunks = new HashMap<>();

        this.storedChunks = new HashSet<>();
        this.storedReps = new ConcurrentHashMap<>();

        this.spaceAvailable = MAX_SPACE;
        this.spaceUsed = 0;
    }

    //GETS
    HashMap<String, File> getFiles() {

        return files;
    }

    HashMap<String, Integer> getDesiredReplicationDegree() {

        return desiredReplicationDegree;
    }

    HashMap<String, ArrayList<Chunk>> getFileChunks() {

        return fileChunks;
    }

    HashSet<Chunk> getStoredChunks() {

        return storedChunks;
    }

    ConcurrentHashMap<String, Integer> getStoredReps() {

        return storedReps;
    }

    long getSpaceAvailable() {

        return spaceAvailable;
    }

    long getOccupiedSpace() {

        spaceUsed = MAX_SPACE - getSpaceAvailable();
        return spaceUsed;
    }

    //ADDS
    void addFile(String fileID, File file) {

        files.put(fileID, file);
    }

    void addDesiredReplicationDegree(String fileID, int replicationDegree) {

        desiredReplicationDegree.put(fileID, replicationDegree);
    }

    void addChunks(String file_id, ArrayList<Chunk> storedChuncks) {

        fileChunks.put(file_id, storedChuncks);
    }

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

    boolean isStoredAlready(Chunk chunk) {

        for (Chunk stored : this.storedChunks) {
            if (stored.getFileID().equals(chunk.getFileID()) && stored.getChunkNr() == chunk.getChunkNr())
                return true;
        }

        return false;
    }

    //DELETES
    void deleteFile(String file_id) {

        files.remove(file_id);
    }

    void deleteDesiredReplicationDegree(String file_id) {

        desiredReplicationDegree.remove(file_id);
    }

    void deleteFileChunks(String file_id) {

        fileChunks.remove(file_id);
    }

    void deleteStoredChunk(String FileId) {

        for (Iterator<Chunk> it = this.storedChunks.iterator(); it.hasNext(); ) {

            Chunk stored = it.next();

            if (stored.getFileID().equals(FileId)) {
                String fileName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + FileId + "/chk" + stored.getChunkNr();
                File file = new File(fileName);
                file.delete();

                //removeReps
                deleteReps(FileId, stored.getChunkNr());

                //freeSpace
                freeSpace(FileId, stored.getChunkNr());

                it.remove();
            }
        }

        String folderName = Peer.getPeerFolder().getAbsolutePath() + "/backup/" + FileId;
        File folder = new File(folderName);
        folder.delete();

    }

    private synchronized void deleteReps(String FileId, int ChunkNr) {

        this.storedReps.remove(FileId + '_' + ChunkNr);
    }

    void reclaimSpace(long spaceClaimed, long spaceUsed) {

        this.spaceAvailable = spaceClaimed - spaceUsed;
        this.spaceUsed = spaceUsed;
    }

    //INCREASE & DECREASE SPACE
    private synchronized void decreaseSpace(int ChunkSize) {

        spaceAvailable -= ChunkSize;
    }

    private synchronized void freeSpace(String FileId, int ChunkNr) {

        for (Chunk stored : this.storedChunks) {
            if (stored.getFileID().equals(FileId) && stored.getChunkNr() == ChunkNr)
                spaceAvailable += stored.getData().length;
        }
    }

    //DECREASE & INCREASE REP DEGREE
    synchronized void decreaseRepDegree(String FileId, int ChunkNr) {

        String key = FileId + '_' + ChunkNr;
        int total = this.storedReps.get(key) - 1;
        this.storedReps.replace(key, total);
    }

    synchronized void increaseRepDegree(String FileId, int ChunkNr) {

        String key = FileId + '_' + ChunkNr;
        int total = this.storedReps.get(key) + 1;
        this.storedReps.replace(key, total);
    }
}