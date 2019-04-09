import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

class ChannelControl extends Channel {

    private volatile HashMap<String, ArrayList<Peer>> stored;

    ChannelControl(InetAddress address, int port) {
        super(address, port);

        stored = new HashMap<>();
    }

    synchronized void startSavingStoredConfirmsFor(String chunkID) {
        if (!stored.containsKey(chunkID))
            stored.put(chunkID, new ArrayList<>());
    }

    synchronized int getNumStoredConfirmsFor(String chunkID) {
        return storedConfirms.get(chunkID).size();
    }

    synchronized void stopSavingStoredConfirmsFor(String chunkID) {
        storedConfirms.remove(chunkID);
    }

};
