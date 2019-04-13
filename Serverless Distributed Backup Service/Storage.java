import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

class Storage implements java.io.Serializable {

    private ArrayList<FileData> files;
    private ArrayList<Chunk> storedChunks;
    private ArrayList<Chunk> receivedChunks;
    private ConcurrentHashMap<String, Integer> storedReps;
    private ConcurrentHashMap<String, String> wantedChunks;
    private int spaceAvailable;
    private long spaceUsed;
    private int MAX_SPACE = 500000;

    public Storage() {
        this.files = new ArrayList<>();
        this.storedChunks = new HashSet<>();
        this.receivedChunks = new ArrayList<>();
        this.storedReps = new ConcurrentHashMap<>();
        this.wantedChunks = new ConcurrentHashMap<>();
        this.spaceAvailable = MAX_SPACE;
    }

    //GETS
    public ArrayList<FileData> getFiles() { return files; }
    public ArrayList<Chunk> getStoredChunks() { return storedChunks; }
    public ArrayList<Chunk> getReceivedChunks() { return receivedChunks; }
    public ConcurrentHashMap<String, Integer> getStoredReps() { return storedReps; }
    public ConcurrentHashMap<String, String> getWantedChunks() { return wantedChunks; }
    public int getSpaceAvailable() { return spaceAvailable; }
    public int getOccupiedSpace() { return MAX_SPACE - getSpaceAvailable(); }

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
    public void deleteStoredChunk(String FileId) {

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

    void reclaimSpace(long spaceClaimed) {
        this.spaceAvailable += spaceClaimed;
        this.spaceUsed -= spaceClaimed;
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