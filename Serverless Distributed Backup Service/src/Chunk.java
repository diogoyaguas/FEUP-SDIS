import java.util.concurrent.TimeUnit;

public class Chunk implements Comparable {

    /**
     * Chunk ID and File ID
     */
    private String ID, fileID;

    /**
     * Information saved
     */
    private byte[] data;

    /**
     * Chunk number, replication degree and current replication degree
     */
    private int nr, repDegree, currentRepDegree;

    /**
     * Instantiates a new Chunk.
     *
     * @param nr        Number of the chunk
     * @param fileID    File ID
     * @param data      Information to be saved
     * @param repDegree Replication Degree
     */
    Chunk(int nr, String fileID, byte[] data, int repDegree) {

        this.nr = nr;
        this.fileID = fileID;
        this.data = data;
        this.repDegree = repDegree;
        this.ID = nr + "_" + fileID;
        this.currentRepDegree = 0;

    }

    /**
     * Get chunk number
     */
    int getChunkNr() {
        return nr;
    }

    /**
     * Get replication degree
     */
    int getRepDegree() {
        return repDegree;
    }

    /**
     * Get current replication degree
     */
    int getCurrentRepDegree() {
        return currentRepDegree;
    }

    /**
     * Get information saved
     */
    byte[] getData() {
        return data;
    }

    /**
     * Get chunk ID
     */
    String getID() {
        return ID;
    }

    /**
     * Get chunk max size
     */
    static int getMaxSize() {
        return 64 * 1000;
    }

    /**
     * Get chunk file ID
     */
    String getFileID() {
        return fileID;
    }

    /**
     * Compare chunks
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;

        Chunk chunk = (Chunk) obj;
        return (nr == chunk.getChunkNr() && fileID.equals(chunk.getFileID()));

    }

    /**
     * Backup a chunk
     */
    void backup() {

        long wait_time = 1;
        int putChunk_sent = 0;
        int stored;

        do {
            Peer.getMDB().startingSaving(this.ID);
            Peer.getMessageForwarder().sendPutChunk(this);
            putChunk_sent++;

            try {
                TimeUnit.SECONDS.sleep(wait_time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            stored = Peer.getMDB().getNumBackups(this.ID);

            wait_time *= 2;

        } while (stored < this.repDegree && putChunk_sent != 5);

        Peer.getMDB().stopSaving(this.ID);
    }

    /**
     * Compare the chunks replication degree
     */
    @Override
    public int compareTo(Object c) {
        return getCurrentRepDegree() - ((Chunk) c).getCurrentRepDegree();
    }

}