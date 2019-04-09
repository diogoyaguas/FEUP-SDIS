import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

class ChannelControl extends Channel {

    private volatile ConcurrentHashMap<String, ArrayList<Integer>> stored;

    ChannelControl(InetAddress address, int port) {
        super(address, port);

        stored = new ConcurrentHashMap<>();
    }

    synchronized void startSavingStoredConfirmsFor(String chunkID) {
        if (!stored.containsKey(chunkID))
            stored.put(chunkID, new ArrayList<>());
    }

    synchronized int getNumStoredConfirmsFor(String chunkID) {
        return stored.get(chunkID).size();
    }

    synchronized void stopSavingStoredConfirmsFor(String chunkID) {
        stored.remove(chunkID);
    }

    public void save(String chunkId, int serverId) {

    }


}
