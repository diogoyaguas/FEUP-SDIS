import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

class ChannelRestore extends Channel {

    private volatile ConcurrentHashMap<String, ArrayList<Chunk>> restored;

    ChannelRestore(InetAddress address, int port) {
        super(address, port);

        restored = new ConcurrentHashMap<>();
    }

    void startRestore(String FileId) {
        restored.put(FileId, new ArrayList<>());
    }

    void stopRestore(String FileId) {
        restored.remove(FileId);
    }

    boolean restoring(String chunkId) {
        return restored.containsKey(chunkId);
    }

    ArrayList<Chunk> getRestored(String FileId) {

        if (restored.containsKey(FileId))
            return restored.get(FileId);

        return null;
    }

    void save(String FileId, Chunk chunk) {
        if (restored.containsKey(FileId))
            restored.get(FileId).add(chunk);
    }

}
