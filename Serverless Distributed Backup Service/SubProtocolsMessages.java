import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

class SubProtocolsMessages {

    /**
     * Process a put chunk message
     *
     * @param fileID
     * @param ChunkNo
     * @param ReplicationDeg
     * @param Body
     */
    static void putChunk(String fileID, int ChunkNo, int ReplicationDeg, byte[] Body) {

        //PUTCHUNK <Version> <SenderId> <fileID> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>

        System.out.println("\nPUTCHUNK received\t");

        if(Peer.getStorage().getFile(fileID) != null) return;

        Chunk chunk = new Chunk(ChunkNo, fileID, Body, ReplicationDeg);

        //SAVE
        Peer.getMDB().save(chunk.getID(), Peer.getPeerID());

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

    /**
     * Process a stored message
     *
     * @param fileID
     * @param ChunkNo
     */
    static void stored(String fileID, int ChunkNo) {

        //STORED <Version> <SenderId> <fileID> <ChunkNo> <CRLF><CRLF>

        System.out.println("\nSTORED received\t");

        Chunk chunk = new Chunk(ChunkNo, fileID, new byte[0], 0);

        if (Peer.getStorage().isStoredAlready(chunk))
            //INCREASE REP DEGREE
            Peer.getStorage().increaseRepDegree(fileID, ChunkNo);

    }

    /**
     * Process a delete message
     *
     * @param fileID
     */
    static void delete(String fileID) {

        //DELETE <Version> <SenderId> <fileID> <CRLF><CRLF>

        System.out.println("\nDELETE received\t");

        Peer.getStorage().deleteStoredChunk(fileID);

    }

    /**
     * Process a removed message
     *
     * @param fileID
     * @param ChunkNo
     */
    static void removed(String fileID, int ChunkNo) {

        //REMOVED <Version> <SenderId> <fileID> <ChunkNo> <CRLF><CRLF>

        System.out.println("\nREMOVED received\t");

        ArrayList<Chunk> chunks = Peer.getStorage().getStoredChunks();

        if (chunks.isEmpty()) return;

        Chunk chunk = new Chunk(ChunkNo, fileID, new byte[0], 0);

        for (Chunk value : chunks) {

            if (value.getFileID().equals(fileID) && value.getChunkNr() == ChunkNo) {
                while (true) {

                    if (value.getID().equals(chunk.getID())) {
                        chunk = value;

                        Peer.getStorage().decreaseRepDegree(fileID, ChunkNo);

                        if (chunk.getCurrentRepDegree() < chunk.getRepDegree()) {

                            // wait a random delay uniformly distributed between 0 and 400 ms
                            Random rand = new Random();
                            int n = rand.nextInt(400) + 1;

                            Peer.getMDB().startingSaving(chunk.getID());

                            try {
                                Thread.sleep(n);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            int save = Peer.getMDB().getNumBackups(chunk.getID());

                            Peer.getMDB().stopSaving(chunk.getID());

                            if (save == 0)
                                chunk.backup();

                        }

                        return;
                    }
                }
            }
        }
    }

    /**
     * Process a get chunk message
     *
     * @param fileID
     * @param ChunkNo
     */
    static void getchunk(String fileID, int ChunkNo) {

        //GETCHUNK <Version> <SenderId> <fileID> <ChunkNo> <CRLF><CRLF>

        System.out.println("\nGETCHUNK received\t");

        File file = new File(Peer.getPeerFolder().getAbsolutePath() + "/backup/" + fileID + "/chk" + ChunkNo);

        Peer.getMDR().startRestore(fileID);

        if (file.exists()) {
            try {

                byte[] dataBody = FileData.loadFile(file);
                Chunk chunk = new Chunk(ChunkNo, fileID, dataBody, 0);

                Random delay = new Random();
                int n = delay.nextInt(400) + 1;

                try {
                    Thread.sleep(n);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ArrayList<Chunk> chunks = Peer.getMDR().getRestored(fileID);

                if (chunks != null) {
                    if (!chunks.contains(new Chunk(ChunkNo, fileID, new byte[0], 0)))
                        Peer.getMessageForwarder().sendChunk(chunk);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Peer.getMDR().stopRestore(fileID);
    }

    /**
     * Process a chunk received message
     *
     * @param fileID
     * @param ChunkNo
     * @param repDegree
     * @param Body
     */
    static void chunk(String fileID, int ChunkNo, int repDegree, byte[] Body) {

        //CHUNK <Version> <SenderId> <fileID> <ChunkNo> <CRLF><CRLF><Body>

        System.out.println("\nCHUNK received\t");

        Chunk chunk = new Chunk(ChunkNo, fileID, Body, repDegree);

        if (Peer.getMDR().restoring(fileID)) {
            Peer.getMDR().save(fileID, chunk);
        }

    }
}