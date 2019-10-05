package protocols;

import chord.Chord_Protocol;
import comms.Message;
import program.NodeKey;

public class DeleteFile implements Runnable {

	private NodeKey nodeKey;
	private String fileID;
	private Chord_Protocol chord;

	public DeleteFile(String fileId, Chord_Protocol chord) {
		this.nodeKey = chord.getPeer().getNodeKey();
		this.fileID = fileId;
		this.chord = chord;
	}

	@Override
	public void run() {
		Message<String> deleteFileMessage = Message.createRequest(Message.Type.DELETE, this.nodeKey + "," + this.fileID,
				chord.getPeer().getAddress());
		chord.getDispatcher().sendMessage(chord.getPeer().getAddress(), deleteFileMessage);
	}

}