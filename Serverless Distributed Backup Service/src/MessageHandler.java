import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.util.Arrays;

//HEADER - <Protocol> <Version> <SenderId> <FileId> <ChunkNr> <RepDegree> <CRLF>

public class MessageHandler implements Runnable {

    private DatagramPacket packet;
    private static String[] parsedHeader;
    private static byte[] body;
    private int chunkNr, repDegree;
    private static String header = "";

    /**
     * Instantiates a new message handler.
     *
     * @param packet Packet with all the information
     */
    MessageHandler(DatagramPacket packet) {
        this.packet = packet;
    }

    /**
     * Method ran when thread starts executing. Process message and execute.
     */
    @Override
    public void run() {

        parsedHeader = parseHeader(packet);
        body = parseBody(packet);
        String subProtocol = parsedHeader[0];
        int serverID = Integer.parseInt(parsedHeader[2]);
        String fileId = parsedHeader[3];

        if (serverID == Peer.getServerId())
            return;

        switch (subProtocol) {
            case "PUTCHUNK":
                chunkNr = Integer.parseInt(parsedHeader[4]);
                repDegree = Integer.parseInt(parsedHeader[5]);
                SubProtocolsMessages.putChunk(fileId, chunkNr, repDegree, body);
                break;

            case "STORED":
                chunkNr = Integer.parseInt(parsedHeader[4]);
                SubProtocolsMessages.stored(fileId, chunkNr, serverID);
                break;

            case "DELETE":
                SubProtocolsMessages.delete(fileId);
                break;

            case "GETCHUNK":
                chunkNr = Integer.parseInt(parsedHeader[4]);
                SubProtocolsMessages.getchunk(fileId, chunkNr);
                break;

            case "CHUNK":
                chunkNr = Integer.parseInt(parsedHeader[4]);
                SubProtocolsMessages.chunk(fileId, chunkNr, body);
                break;

            case "REMOVED":
                chunkNr = Integer.parseInt(parsedHeader[4]);
                SubProtocolsMessages.removed(fileId, chunkNr);
                break;

            default:
                break;
        }

    }

    /**
     * Process a message header.
     *
     * @param packet Packet to process
     */
    private String[] parseHeader(DatagramPacket packet) {

        header = "";

        ByteArrayInputStream input = new ByteArrayInputStream(packet.getData());
        BufferedReader output = new BufferedReader(new InputStreamReader(input));

        try {
            header = output.readLine();
        } catch (Exception e) {
            System.err.println("\nParseHeader Exception: " + e.toString());
            e.printStackTrace();
        }

        return header.split(" ");
    }

    /**
     * Process a message body.
     *
     * @param packet Packet to process
     */
    private static byte[] parseBody(DatagramPacket packet) {

        ByteArrayInputStream input = new ByteArrayInputStream(packet.getData());
        BufferedReader output = new BufferedReader(new InputStreamReader(input));

        header = "";

        try {
            header += output.readLine();
        } catch (Exception e) {
            System.err.println("\nParseBody Exception: " + e.toString());
            e.printStackTrace();
        }

        int bodyIndex = header.length() + 2 * MessageForwarder.CRLF.length();

        if(bodyIndex > packet.getLength()) {
            bodyIndex = packet.getLength();
        }

        body = Arrays.copyOfRange(packet.getData(), bodyIndex, packet.getLength());

        return body;
    }
}
