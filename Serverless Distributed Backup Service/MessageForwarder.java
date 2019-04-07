public class MessageForwarder {
    private double protocolVersion;

    private static byte CR = 0xD;
    private static byte LF = 0xA;
    private static String CRLF = (char) CR + "" + (char) LF;

    public MessageForwarder(double protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    private byte[] createMessage(byte[] header, byte[] body) {

        byte[] msg = new byte[header.length + body.length];
        System.arraycopy(header, 0, msg, 0, header.length);
        System.arraycopy(body, 0, msg, header.length, body.length);

        return msg;
    }

    // The initiator-peer sends to the MDB
    // multicast data channel a message
    public void sendPutChunk(Chunk chunk) {
        String header = "PUTCHUNK" + " " + protocolVersion + " " + Peer.getServerId() + " " + chunk.getFileID()
                + " " + chunk.getChunkNr() + " " + chunk.getRepDegree() + " " + CRLF + CRLF;

        byte[] msg = createMessage(header.getBytes(), chunk.getData());
        Peer.getMDB().sendMessage(msg);

    }

    // Reply by sending on the MC a confirmation message
    public void sendStored(Chunk chunk) {
        String header = "STORED" + " " + protocolVersion + " " + Peer.getServerId() + " " + chunk.getFileID()
                + " " + chunk.getChunkNr() + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());
    }

    // The initiator-peer shall send a message to the MC
    public void sendGetChunk(int chunk_no, String file_id) {
        String header = "GETCHUNK" + " " + protocolVersion + " " + Peer.getServerId() + " " + file_id
                + " " + chunk_no + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());

    }

    // Send the body of a CHUNK message via the MDR channel
    public void sendChunk(Chunk chunk) {
        String header = "CHUNK" + " " + protocolVersion + " " + Peer.getServerId() + " " + chunk.getFileID()
                + " " + chunk.getChunkNr() + " " + chunk.getRepDegree() + " " + CRLF + CRLF;

        byte[] msg = createMessage(header.getBytes(), chunk.getData());
        Peer.getMDR().sendMessage(msg);

    }

    // Chunks should be deleted from the backup service
    public void sendDelete(String file_id) {
        String header = "DELETE" + " " + protocolVersion + " " + Peer.getServerId()
                + " " + file_id + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());
    }

    // Deletes a copy of a chunk it has backed up, sending to the MC channel
    public void sendRemoved(int chunk_no, String file_id) {
        String header = "REMOVED" + " " + protocolVersion + " " + Peer.getServerId() + " " + file_id
                + " " + chunk_no + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());

    }

}