import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Storage implements java.io.Serializable {

    private ArrayList<FileData> files;
    private ArrayList<Chunk> storedChunks;
    private ArrayList<Chunk> receivedChunks;
    private ConcurrentHashMap<String, Integer> storedReps;
    private ConcurrentHashMap<String, String> wantedChunks;
    private int spaceAvailable;
    private int MAX_SPACE = 1000000000;

    public Storage() {
        this.files = new ArrayList<>();
        this.storedChunks = new ArrayList<>();
        this.receivedChunks = new ArrayList<>();
        this.storedReps = new ConcurrentHashMap<>();
        this.wantedChunks = new ConcurrentHashMap<>();
        this.spaceAvailable = MAX_SPACE;
    }

    //GETS
    public ArrayList<FileData> getFiles() {
        return files;
    }

    public ArrayList<Chunk> getStoredChunks() {
        return storedChunks;
    }

    public ArrayList<Chunk> getReceivedChunks() {
        return receivedChunks;
    }

    public ConcurrentHashMap<String, Integer> getStoredReps() {
        return storedReps;
    }

    public ConcurrentHashMap<String, String> getWantedChunks() {
        return wantedChunks;
    }

    private int getSpaceAvailable() {
        return spaceAvailable;
    }

    public int getOccupiedSpace() {
        return MAX_SPACE - getSpaceAvailable();
    }

    //SETS
    public synchronized void setSpaceAvailable(int spaceAvailable) {
        this.spaceAvailable = spaceAvailable;
    }

    public void setWantedChunkReceived(String FileId, int ChunkNr) {
        this.wantedChunks.put(FileId + '_' + ChunkNr, "true");
    }

    public void setStoredChunkRepDegree() {

        for (Chunk stored : this.storedChunks) {
            String key = stored.getFileID() + "_" + stored.getChunkNr();
            stored.setCurrentRepDegree(this.storedReps.get(key));
        }
    }

    //ADDS
    public void addFile(FileData file) {
        this.files.add(file);
    }

    public void addWantedChunk(String FileId, int ChunkNr) {
        this.wantedChunks.put(FileId + '_' + ChunkNr, "false");
    }

    //Makes sure the added chunk isn't already there and adds it
    synchronized void addStoredChunk(Chunk chunk) {

        if (!isStoredAlready(chunk))
            this.storedChunks.add(chunk);
    }

    boolean isStoredAlready(Chunk chunk) {

        for (Chunk stored : this.storedChunks) {
            if (stored.getFileID().equals(chunk.getFileID()) && stored.getChunkNr() == chunk.getChunkNr())
                return true;
        }

        return false;
    }

    //DELETES
    public void deleteStoredChunk(String FileId) {

        //if there is an error, maybe here - tentei inovar
        for (Iterator<Chunk> it = this.storedChunks.iterator(); it.hasNext(); ) {

            Chunk stored = it.next();

            if (stored.getFileID().equals(FileId)) {
                String fileName = Peer.getServerId() + "/" + FileId + "_" + stored.getChunkNr();
                File file = new File(fileName);
                file.delete();

                //removeReps
                deleteReps(FileId, stored.getChunkNr());

                //freeSpace
                freeSpace(FileId, stored.getChunkNr());

                it.remove();
            }
        }
    }

    private synchronized void deleteReps(String FileId, int ChunkNr) {
        this.storedReps.remove(FileId + '_' + ChunkNr);
    }

    //INCREASE & DECREASE SPACE
    public synchronized void decreaseSpace(int ChunkSize) {
        spaceAvailable -= ChunkSize;
    }

    private synchronized void freeSpace(String FileId, int ChunkNr) {

        for (Chunk stored : this.storedChunks) {
            if (stored.getFileID().equals(FileId) && stored.getChunkNr() == ChunkNr)
                spaceAvailable += stored.getData().length;
        }
    }

    //DECREASE & INCREASE REP DEGREE
    public synchronized void decreaseRepDegree(String FileId, int ChunkNr) {
        String key = FileId + '_' + ChunkNr;
        int total = this.storedReps.get(key) - 1;
        this.storedReps.replace(key, total);
    }

    synchronized void increaseRepDegree(String FileId, int ChunkNr) {
        String key = FileId + '_' + ChunkNr;
        int total = this.storedReps.get(key) + 1;
        this.storedReps.replace(key, total);
    }


};