import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

class Storage implements java.io.Serializable {

    private HashSet<Chunk> storedChunks;
    private ConcurrentHashMap<String, Integer> storedReps;
    private long spaceAvailable;
    private long spaceUsed;
    private long MAX_SPACE = 500000;

    Storage() {
        this.storedChunks = new HashSet<>();
        this.storedReps = new ConcurrentHashMap<>();
        this.spaceAvailable = MAX_SPACE;
        this.spaceUsed = 0;
    }

    HashSet<Chunk> getStoredChunks() {

        return this.storedChunks;
    }

    long getSpaceAvailable() {
        return spaceAvailable;
    }

    long getSpaceUsed() {
        return spaceUsed;
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
            String key = "chk" + chunk.getChunkNr();
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

    void reclaimSpace(long spaceClaimed) {
        this.spaceAvailable += spaceClaimed;
        this.spaceUsed -= spaceClaimed;
    }

    //INCREASE & DECREASE SPACE
    private synchronized void decreaseSpace(int ChunkSize) {
        spaceAvailable -= ChunkSize;
        spaceUsed += ChunkSize;
    }

    private synchronized void freeSpace(String FileId, int ChunkNr) {

        for (Chunk stored : this.storedChunks) {
            if (stored.getFileID().equals(FileId) && stored.getChunkNr() == ChunkNr) {
                spaceAvailable += stored.getData().length;
                spaceUsed -= stored.getData().length;
            }
        }
    }

    //INCREASE REP DEGREE
    synchronized void increaseRepDegree(int ChunkNr) {
        String key = "chk" + ChunkNr;
        int total = this.storedReps.get(key) + 1;
        this.storedReps.replace(key, total);
    }

    synchronized void decreaseRepDegree(int ChunkNr) {
        String key = "chk" + ChunkNr;
        int total = this.storedReps.get(key) - 1;
        this.storedReps.replace(key, total);
    }
}