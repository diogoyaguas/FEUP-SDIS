package chord;

import comms.Dispatcher;
import comms.Message;
import program.NodeKey;

import java.net.InetSocketAddress;

public class Check_Predecessor implements Runnable {

    private Dispatcher dispatcher;
    private Chord_Protocol chord;

    Check_Predecessor(Chord_Protocol chord, Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        this.chord = chord;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        InetSocketAddress predecessor = chord.getPredecessor();

        if (chord.getPredecessor() == null) {
            System.err.println("Predecessor not set\n");
            return;
        }

        Message message = Message.createRequest(Message.Type.NODEKEY, null, chord.getPeer().getAddress());
        Message response = dispatcher.sendMessage(predecessor, message);

        if (response == null) {
            chord.setPredecessor(null);
        } else if (response.getArgument().equals(NodeKey.keyFromAddress(predecessor))) {
            System.out.println("Predecessor" + predecessor + "is still active!");
        } else {
            System.err.println("Got predecessor without valid key");
        }

    }
}