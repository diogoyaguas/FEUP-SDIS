public class Chunk  implements Comparable {

    private String  ID, fileID;
    private byte[] data;
    private int nr, repDegree, currentRepDegree;

    public Chunk(int nr, String fileID, byte[] data, int repDegree) {

        this.nr = nr;
        this.fileID = fileID;
        this.data = data;
        this.repDegree = repDegree;
        this.ID = nr + "_" + fileID;
        this.currentRepDegree = 0;

    }

    //GETS
    public int getChunkNr() { return nr;}
    public int getRepDegree() { return repDegree;}
    public int getCurrentRepDegree() { return currentRepDegree;}
    public byte[] getData() { return data;}
    public String getID() { return ID;}
    public String getFileID() { return fileID;}

    //SETS
    public void setCurrentRepDegree(int replica) { currentRepDegree = replica;}
    public void setRepDegree(int replications) { repDegree = replications;}

    @Override 
    public int compareTo(Object c) {
        return getCurrentRepDegree() - ((Chunk)c).getCurrentRepDegree();
    }
    
}