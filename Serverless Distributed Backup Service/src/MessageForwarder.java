class MessageForwarder {
    private double protocolVersion;

    private static byte CR = 0xD;
    private static byte LF = 0xA;
    static String CRLF = (char) CR + "" + (char) LF;

    /**
     * Instantiates a new message forwarder.
     *
     * @param protocolVersion Version of Protocol
     */
    MessageForwarder(double protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    /**
     * Create a new message
     *
     * @param header Message's header
     * @param body   Messages's body
     */
    private byte[] createMessage(byte[] header, byte[] body) {

        byte[] msg = new byte[header.length + body.length];
        System.arraycopy(header, 0, msg, 0, header.length);
        System.arraycopy(body, 0, msg, header.length, body.length);

        return msg;
    }

    /**
     * Send a new Put chunk message. The initiator-peer sends to the MDB multicast data channel a message.
     *
     * @param chunk Chunk
     */
    void sendPutChunk(Chunk chunk) {
        String header = "PUTCHUNK" + " " + protocolVersion + " " + Peer.getServerId() + " " + chunk.getFileID()
                + " " + chunk.getChunkNr() + " " + chunk.getRepDegree() + " " + CRLF + CRLF;

        byte[] msg = createMessage(header.getBytes(), chunk.getData());
        Peer.getMDB().sendMessage(msg);

    }

    /**
     * Send a new stored chunk message. Reply by sending on the MC a confirmation message.
     *
     * @param chunk Chunk
     */
    void sendStored(Chunk chunk) {
        String header = "STORED" + " " + protocolVersion + " " + Peer.getServerId() + " " + chunk.getFileID()
                + " " + chunk.getChunkNr() + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());
    }

    /**
     * Send a new get chunk message. The initiator-peer shall send a message to the MC.
     *
     * @param chunk_no Chunk number
     * @param file_id  File ID
     */
    void sendGetChunk(int chunk_no, String file_id) {
        String header = "GETCHUNK" + " " + protocolVersion + " " + Peer.getServerId() + " " + file_id
                + " " + chunk_no + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());

    }

    /**
     * Send a new chunk message. Send the body of a CHUNK message via the MDR channel.
     *
     * @param chunk Chunk
     */
    void sendChunk(Chunk chunk) {
        String header = "CHUNK" + " " + protocolVersion + " " + Peer.getServerId() + " " + chunk.getFileID()
                + " " + chunk.getChunkNr() + " " + chunk.getRepDegree() + " " + CRLF + CRLF;

        byte[] msg = createMessage(header.getBytes(), chunk.getData());
        Peer.getMDR().sendMessage(msg);

    }

    /**
     * Send a new chunk message. Chunks should be deleted from the backup service.
     *
     * @param file_id File ID
     */
    void sendDelete(String file_id) {
        String header = "DELETE" + " " + protocolVersion + " " + Peer.getServerId()
                + " " + file_id + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());
    }

    /**
     * Send a new chunk message. Deletes a copy of a chunk it has backed up, sending to the MC channel.
     *
     * @param chunk_no Chunk number
     * @param file_id  File ID
     */
    void sendRemoved(int chunk_no, String file_id) {
        String header = "REMOVED" + " " + protocolVersion + " " + Peer.getServerId() + " " + file_id
                + " " + chunk_no + " " + CRLF + CRLF;

        Peer.getMC().sendMessage(header.getBytes());

    }

}