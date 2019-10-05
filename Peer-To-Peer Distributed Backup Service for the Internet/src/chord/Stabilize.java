package chord;

import comms.Message;
import program.NodeKey;

import java.net.InetSocketAddress;

public class Stabilize implements Runnable {
    private Chord_Protocol chord_protocol;

    Stabilize(Chord_Protocol chord_protocol) {
        this.chord_protocol = chord_protocol;
    }

    @SuppressWarnings("unchecked") // Random warning not allowed to compile
    @Override
    public void run() {

        InetSocketAddress successor = chord_protocol.getSuccessor();

        if (successor == null) {
            System.err.println("This node successor is null");
            return;
        }

        Message request = Message.createRequest(Message.Type.PREDECESSOR, null, chord_protocol.getPeer().getAddress());
        InetSocketAddress possible_predecessor = chord_protocol.getDispatcher().requestAddress(successor, request);

        if (possible_predecessor == null) {
            System.err.println("Successor's predecessor is null");
            return;
        }

        NodeKey successor_key = NodeKey.keyFromAddress(successor);
        NodeKey possible_pre_key = NodeKey.keyFromAddress(possible_predecessor);

        if (possible_pre_key.isBetween(chord_protocol.getPeer().getNodeKey(), successor_key)
                || chord_protocol.getPeer().getNodeKey() == successor_key)
            chord_protocol.setIthFinger(0, possible_predecessor);

        chord_protocol.notify(chord_protocol.getSuccessor());
    }
}