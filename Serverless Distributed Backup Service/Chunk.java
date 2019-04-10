import java.util.concurrent.TimeUnit;

public class Chunk implements Comparable {

    private static int MAX_SIZE = 64 * 1000;
    private String ID, fileID;
    private byte[] data;
    private int nr, repDegree, currentRepDegree;

    Chunk(int nr, String fileID, byte[] data, int repDegree) {

        this.nr = nr;
        this.fileID = fileID;
        this.data = data;
        this.repDegree = repDegree;
        this.ID = nr + "_" + fileID;
        this.currentRepDegree = 0;

    }

    //GETS
    int getChunkNr() {
        return nr;
    }

    int getRepDegree() {
        return repDegree;
    }

    private int getCurrentRepDegree() {
        return currentRepDegree;
    }

    byte[] getData() {
        return data;
    }

    String getID() {
        return ID;
    }

    static int getMaxSize() {
        return MAX_SIZE;
    }

    String getFileID() {
        return fileID;
    }

    //SETS
    void setCurrentRepDegree(int replica) {
        currentRepDegree = replica;
    }

    public void setRepDegree(int replications) {
        repDegree = replications;
    }

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

    @Override
    public int compareTo(Object c) {
        return getCurrentRepDegree() - ((Chunk) c).getCurrentRepDegree();
    }

}