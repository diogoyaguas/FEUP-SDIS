import java.net.InetAddress;
import java.util.HashSet;
import java.util.Hashtable;

class ChannelBackup extends ChannelControl {

    private Hashtable<String, HashSet<Integer>> backup;

    ChannelBackup(InetAddress address, int port) {
        super(address, port);
        backup = new Hashtable<>();
    }

    synchronized void startingSaving(String chunk_id) {
        backup.put(chunk_id, new HashSet<>());
    }

    synchronized int getNumBackups(String chunkID) {
        return backup.get(chunkID).size();
    }

    synchronized void save(String chunk_id, int peer_id) {
        if (backup.containsKey(chunk_id))
            backup.get(chunk_id).add(peer_id);
    }

    synchronized void stopSaving(String chunkID) {
        backup.remove(chunkID);
    }

}
