import java.net.InetAddress;
import java.util.HashSet;
import java.util.Hashtable;

class ChannelBackup extends ChannelControl {

    /**
     * The channel's chunk's ID.
     */
    private Hashtable<String, HashSet<Integer>> backup;

    /**
     * Instantiates a new Channel Backup.
     *
     * @param address the address
     * @param port    the port
     */
    ChannelBackup(InetAddress address, int port) {
        super(address, port);
        backup = new Hashtable<>();
    }

    /**
     * Create a HashSet to the chunk's ID indicated
     *
     * @param chunk_id Chunk's ID
     */
    synchronized void startingSaving(String chunk_id) {
        backup.put(chunk_id, new HashSet<>());
    }

    /**
     * Return number of peers
     *
     * @param chunkID Chunk's ID
     */
    synchronized int getNumBackups(String chunkID) {

        if(backup.get(chunkID) != null) {
            return backup.get(chunkID).size();
        }
        return 0;
    }

    /**
     * Save peer's ID
     *
     * @param chunk_id Chunk's ID
     * @param peer_id  Peer's ID
     */
    synchronized void save(String chunk_id, int peer_id) {
        if (backup.containsKey(chunk_id))
            backup.get(chunk_id).add(peer_id);
    }

    /**
     * Remove all Peers from Chunk
     *
     * @param chunkID Chunk's ID
     */
    synchronized void stopSaving(String chunkID) {
        backup.remove(chunkID);
    }

}
