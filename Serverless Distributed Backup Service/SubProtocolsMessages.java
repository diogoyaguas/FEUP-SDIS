import java.util.Random;

class SubProtocolsMessages {

    //IF ERROR - MAYBE NOT STATIC?
    static void putchunk(String FileId, int ChunkNo, int ReplicationDeg, byte[] Body) {
        //PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>

        System.out.println("PUTCHUNK RECEIVED\t");

        Chunk chunk = new Chunk(ChunkNo, FileId, Body, ReplicationDeg);

        //SAVE


        Peer.getStorage().addStoredChunk(chunk);

        Peer.getMC().startSavingStoredConfirmsFor(chunk.getID());

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
        System.out.println("STORED RECEIVED\t");

        String chunkId = ChunkNo + "_" + FileId;
        Chunk chunk = new Chunk(ChunkNo, FileId, new byte[0], 0);

        //SAVE
        Peer.getMC();

        if (Peer.getStorage().isStoredAlready(chunk))
            //INCREASE REP DEGREE
            Peer.getStorage().increaseRepDegree(FileId, ChunkNo);

    }

    static void delete() {
        //DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>

        System.out.println("DELETE RECEIVED\t");

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