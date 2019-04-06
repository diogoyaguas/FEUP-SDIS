package Channel;

import Peer.Chunk;
import Peer.Peer;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

public class ChannelControl extends Channel {

    private volatile HashMap<Chunk, ArrayList<Peer>> storedConfirms;

    public ChannelControl(InetAddress address, int port) {
        super(address, port);

        storedConfirms = new HashMap<Chunk, ArrayList<Peer>>();
    }

}
