import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

class ChannelRestore extends Channel {

    /**
     * Chunks saved to be Restored
     */
    private volatile ConcurrentHashMap<String, ArrayList<Chunk>> restored;

    /**
     * Instantiates a new Channel Restore.
     *
     * @param address the address
     * @param port    the port
     */
    ChannelRestore(InetAddress address, int port) {
        super(address, port);

        restored = new ConcurrentHashMap<>();
    }

    /**
     * Create ArrrayList to save Chunks
     *
     * @param FileId File ID
     */
    void startRestore(String FileId) {
        restored.put(FileId, new ArrayList<>());
    }

    /**
     * Remove all chunks
     *
     * @param FileId File ID
     */
    void stopRestore(String FileId) {
        restored.remove(FileId);
    }

    /**
     * Confirms if exist chunk with ID
     *
     * @param chunkId Chunk's ID
     */
    boolean restoring(String chunkId) {
        return restored.containsKey(chunkId);
    }

    /**
     * Return all chunks restored
     *
     * @param FileId File ID
     */
    ArrayList<Chunk> getRestored(String FileId) {

        if (restored.containsKey(FileId))
            return restored.get(FileId);

        return null;
    }

    /**
     * Save chunk
     *
     * @param FileId File ID
     * @param chunk  Chunk
     */
    void save(String FileId, Chunk chunk) {
        if (restored.containsKey(FileId))
            restored.get(FileId).add(chunk);
    }

}
