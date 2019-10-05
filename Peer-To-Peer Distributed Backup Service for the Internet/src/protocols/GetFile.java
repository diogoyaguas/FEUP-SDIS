package protocols;

import chord.Chord_Protocol;
import program.NodeKey;
import comms.Message;

public class GetFile implements Runnable {

	private NodeKey nodeKey;
	private String fileID;
	private String filename;
	private Chord_Protocol chord;

	public GetFile(String fileID, String filepath, Chord_Protocol chord) {
		this.nodeKey = chord.getPeer().getNodeKey();
		this.fileID = fileID;
		this.filename = filepath;
		this.chord = chord;
	}

	@Override
	public void run() {
		Message<String> getFileMessage = Message.createRequestWithData(Message.Type.GET,
				this.nodeKey + "," + this.fileID + "," + this.filename, chord.getPeer().getAddress(), null);
		chord.getDispatcher().sendMessage(chord.findSuccessor(nodeKey), getFileMessage);
	}

}