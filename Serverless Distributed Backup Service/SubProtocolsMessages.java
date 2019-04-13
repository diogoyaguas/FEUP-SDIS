import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

class SubProtocolsMessages {

    static void putChunk(String FileId, int ChunkNo, int ReplicationDeg, byte[] Body) {

        //PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>

        System.out.println("\nPUTCHUNK received\t");

        Chunk chunk = new Chunk(ChunkNo, FileId, Body, ReplicationDeg);

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

    static void stored(String FileId, int ChunkNo) {

        //STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

        System.out.println("\nSTORED received\t");

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

        System.out.println("\nDELETE received\t");

        Peer.getStorage().deleteStoredChunk(fileId);

    }

    static void removed(String FileId, int ChunkNo) {

        //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

   /*     System.out.println("\nREMOVED received\t");

        Chunk chunk = new Chunk(ChunkNo, FileId, new byte[0], 0);

        HashSet<Chunk> chunks = Peer.getStorage().getStoredChunks();

        if (chunks.contains(chunk)) {

            System.out.println("Contains");
            Iterator iter = chunks.iterator();
            Chunk chunk_iter = (Chunk) iter.next();
            while (true) {

                if (chunk_iter.getID().equals(chunk.getID())) {
                    chunk = chunk_iter;

                    Peer.getStorage().decreaseRepDegree(FileId, ChunkNo);
                    chunk.setCurrentRepDegree(chunk.getCurrentRepDegree() - 1);

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

                        System.out.println("SAVES " + save);

                        Peer.getMDB().stopSaving(chunk.getID());

                        if (save == 0)
                            chunk.backup();

                    }
                    return;
                }
            }
        }*/
    }

    //R E S T O R E
    static void getchunk(int senderId, String fileId, int ChunkNo) {

        //GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>

        System.out.println("\nGETCHUNK received\t");

        File file = new File(Peer.getPeerFolder().getAbsolutePath() + "/backup/" + fileId + "/chk" + ChunkNo);

        Peer.getMDR().startRestore(fileId);

        if (file.exists()) {
            try {

                byte[] dataBody = FileData.loadFile(file);
                Chunk chunk = new Chunk(ChunkNo, fileId, dataBody, 0);

                Random delay = new Random();
                int n = delay.nextInt(400) + 1;

                try {
                    Thread.sleep(n);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                ArrayList<Chunk> chunks = Peer.getMDR().getRestored(fileId);

                if (chunks != null) {
                    if (!chunks.contains(new Chunk(ChunkNo, fileId, new byte[0], 0)))
                        Peer.getMessageForwarder().sendChunk(chunk);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Peer.getMDR().stopRestore(fileId);
    }

    static void chunk(String FileId, int ChunkNo, int repDegree, byte[] Body) {

        //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>

        System.out.println("\nCHUNK received\t");

        Chunk chunk = new Chunk(ChunkNo, FileId, Body, repDegree);

        if (Peer.getMDR().restoring(FileId)) {
            Peer.getMDR().save(FileId, chunk);
        }

    }
}