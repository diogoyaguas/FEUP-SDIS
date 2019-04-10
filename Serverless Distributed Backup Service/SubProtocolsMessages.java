import java.util.Random;

class SubProtocolsMessages {

    //IF ERROR - MAYBE NOT STATIC?
    static void putChunk(String FileId, int ChunkNo, int ReplicationDeg, byte[] Body) {
        //PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>

        System.out.println("\nPUTCHUNK RECEIVED\t");

        Chunk chunk = new Chunk(ChunkNo, FileId, Body, ReplicationDeg);

        //SAVE
        Peer.getMDB().save(chunk.getID(), 0);

        Peer.getStorage().addStoredChunk(chunk);

        Peer.getMDB().startingSaving(chunk.getID());

        Random rand = new Random();
        int n = rand.nextInt(400) + 1;

        try {
            Thread.sleep(n);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Peer.getMessageForwarder().sendStored(chunk);

    }

    static void stored(int senderId, String FileId, int ChunkNo) {
        //STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        System.out.println("\nSTORED RECEIVED\t");

        String chunkId = ChunkNo + "_" + FileId;
        Chunk chunk = new Chunk(ChunkNo, FileId, new byte[0], 0);

        //SAVE
        Peer.getMC().startSavingStoredConfirmsFor(chunkId);

        if (Peer.getStorage().isStoredAlready(chunk))
            //INCREASE REP DEGREE
            Peer.getStorage().increaseRepDegree(FileId, ChunkNo);

    }

    static void delete(String fileId) {
        //DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>
        System.out.println("\nDELETE RECEIVED\t");

        Peer.getStorage().deleteStoredChunk(fileId);

    }

    static void removed() {
        //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

        System.out.println("REMOVED RECEIVED\t");

    }

    //R E S T O R E
    static void getchunk() {
        //GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

        System.out.println("GETCHUNK RECEIVED\t");

    }

    static void chunk(String FileId, int ChunkNo, int repDegree, byte[] Body) {
        //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>

        System.out.println("CHUNK RECEIVED\t");


    }
};