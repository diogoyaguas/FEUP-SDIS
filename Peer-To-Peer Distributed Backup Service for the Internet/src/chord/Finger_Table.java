package chord;

import java.net.InetSocketAddress;

import program.NodeKey;

public class Finger_Table implements Runnable {

    private Chord_Protocol chord;

    Finger_Table(Chord_Protocol chord) {
        this.chord = chord;
    }

    @Override
    public void run() {
        updateFingerTable();
        printFingerTable();
    }

    private void printFingerTable() {

        StringBuilder m = new StringBuilder();

        for (int i = 0; i < chord.getFingerTable().length(); i++) {
            InetSocketAddress entry = chord.getFingerTable().get(i);
            if (entry != null)
                m.append("\t").append(NodeKey.keyFromAddress(entry).toString()).append("\n");
        }

        System.out.println("Finger Table Node: " + chord.getPeer().getNodeKey().toString() + "\n" + m);
    }

    private void updateFingerTable() {

        for (int i = 0; i < Chord_Protocol.get_M(); i++) {

            int nodeKey = Integer.parseInt(chord.getPeer().getNodeKey().toString());
            int keySucessor = nodeKey + 1 << i;
            InetSocketAddress sucessor = chord.findSuccessor(new NodeKey(keySucessor));
            chord.setIthFinger(i, sucessor);
        }

    }
}
