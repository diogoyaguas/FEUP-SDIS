package protocols;

import chord.Chord_Protocol;
import comms.Message;
import program.NodeKey;

public class ReclaimSpace implements Runnable {

    private Chord_Protocol chord;
    private int bytes_wanted;

    public ReclaimSpace(int bytes_wanted, Chord_Protocol chord) {
        this.chord = chord;
        this.bytes_wanted = bytes_wanted;
    }

    @Override
    public void run() {
        Message<Integer> reclaim_request = Message.createRequest(Message.Type.RECLAIM, this.bytes_wanted,
                chord.getPeer().getAddress());
        chord.getDispatcher().sendMessage(chord.getPeer().getAddress(), reclaim_request);
        
    }

}