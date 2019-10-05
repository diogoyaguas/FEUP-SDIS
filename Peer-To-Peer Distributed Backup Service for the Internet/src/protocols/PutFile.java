package protocols;

import java.util.Arrays;
import chord.Chord_Protocol;
import program.NodeKey;
import comms.Message;

public class PutFile implements Runnable {

	private NodeKey nodeKey;
	private String fileID;
	private int replicationDegree;
	private byte[] body;
	private Chord_Protocol chord;

	public PutFile(String fileID, byte[] body, int replicationDegree, Chord_Protocol chord) {

		this.nodeKey = chord.getPeer().getNodeKey();
		this.fileID = fileID;
		this.replicationDegree = replicationDegree;
		this.body = Arrays.copyOf(body, body.length);
		this.chord = chord;
	}

	@Override
	public void run() {
		Message<String> put_File_message = Message.createRequestWithData(Message.Type.PUT,
				this.nodeKey + "," + this.fileID + "," + this.replicationDegree + "," + this.replicationDegree,
                chord.getPeer().getAddress(), this.body);
                // System.out.println("SENT PUUUUUUUUUUUUT");
		chord.getDispatcher().sendMessage(chord.findSuccessor(nodeKey), put_File_message);
	}
}