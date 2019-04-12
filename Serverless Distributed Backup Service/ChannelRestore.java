import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

class ChannelRestore extends Channel {

    //not sure if chunk or integer
    private volatile ConcurrentHashMap<String, ArrayList<Chunk>> restored;

    ChannelRestore(InetAddress address, int port) {
        super(address, port);

        restored = new ConcurrentHashMap<String, ArrayList<Chunk>>();
    }

    public void startRestore(String FileId){ restored.put(FileId, new ArrayList<Chunk>()); }
    public void stopRestore(String FileId){ restored.remove(FileId); }
    public boolean restoring(String chunkId){ return restored.containsKey(chunkId);}
    public ArrayList<Chunk> getRestored(String FileId){

        if(restored.containsKey(FileId))
            return restored.get(FileId);

        return null;
    }

    public void save(String FileId, Chunk chunk){

        if(restored.containsKey(FileId))
            restored.get(FileId).add(chunk);
    }

}
