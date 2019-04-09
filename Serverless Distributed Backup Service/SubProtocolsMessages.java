import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.Random;

public class SubProtocolsMessages {

    //IF ERROR - MAYBE NOT STATIC?
   public static void putchunk(String FileId, int ChunkNo, int ReplicationDeg, byte[] Body) {
    //PUTCHUNK <Version> <SenderId> <FileId> <ChunkNo> <ReplicationDeg> <CRLF><CRLF><Body>
    System.out.println("PUTCHUNK RECEIVED\t");

    Chunk chunk = new Chunk(ChunkNo, FileId, Body, ReplicationDeg);

    //SAVE


    //not sure this is right
    Storage.addStoredChunk(chunk);

    Peer.getMC().startSavingStoredConfirmsFor(chunk.getID());

    Random rand = new Random();
    int n = rand.nextInt(400) + 1;

    try{
        Thread.sleep(n);
    } catch(InterruptedException e){
        e.printStackTrace();
    }

    Peer.getMessageForwarder().sendStored(chunk);
    
    }
          
    public static void stored(int senderId, String FileId, int ChunkNo){
    //STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    System.out.println("STORED RECEIVED\t");

    String chunkId = ChunkNo + "_" + FileId;
    Chunk chunk = new Chunk(ChunkNo, FileId, new byte[0], 0);

     //SAVE
    Peer.getMC();

    if(Storage.isStoredAlready(chunk))
     //INCREASE REP DEGREE

    }
    
    public static void delete(){
    //DELETE <Version> <SenderId> <FileId> <CRLF><CRLF>

    System.out.println("DELETE RECEIVED\t");
        
    }

    public static void removed(){
    //REMOVED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
    
    System.out.println("REMOVED RECEIVED\t");
    
    }

    //R E S T O R E
    public static void getchunk(){
    //GETCHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>
        
    System.out.println("GETCHUNK RECEIVED\t");
    
    }

    public static void chunk(String FileId, int ChunkNo, int repDegree, byte[] Body){
    //CHUNK <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF><Body>
        
    System.out.println("CHUNK RECEIVED\t");


        
    }
};