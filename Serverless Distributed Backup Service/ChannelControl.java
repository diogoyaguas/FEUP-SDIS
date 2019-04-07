import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

class ChannelControl extends Channel {

    private volatile HashMap<String, ArrayList<Peer>> storedConfirms;

    ChannelControl(InetAddress address, int port) {
        super(address, port);

        storedConfirms = new HashMap<>();
    }

    synchronized void startSavingStoredConfirmsFor(String chunkID) {
        if (!storedConfirms.containsKey(chunkID))
            storedConfirms.put(chunkID, new ArrayList<>());
    }

    public synchronized int getNumStoredConfirmsFor(String chunkID) {
        return storedConfirms.get(chunkID).size();
    }

    public synchronized void stopSavingStoredConfirmsFor(String chunkID) {
        storedConfirms.remove(chunkID);
    }

}
