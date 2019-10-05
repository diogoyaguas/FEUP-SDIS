package comms;

import program.NodeKey;
import program.Peer;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dispatcher extends Thread {

    private Peer peer;
    private ExecutorService thread_pool;

    public Dispatcher(Peer peer) {
        this.peer = peer;
        this.thread_pool = Executors.newFixedThreadPool(5);
    }

    public <S extends Serializable> InetSocketAddress requestAddress(InetSocketAddress address, Message<S> message) {

        Message msg = sendMessage(address, message);
        InetSocketAddress wanted_address = null;

        try {

            wanted_address = (InetSocketAddress) msg.getArgument();

        } catch (ClassCastException e) {
            System.err.println("Failed to cast argument to InetSocketAddress." + e.getMessage());
        }

        return wanted_address;
    }

    public <S extends Serializable> Message sendMessage(InetSocketAddress address, Message<S> message) {

        SSLSocket socket;

        try {
            socket = createSocket(address.getAddress(), address.getPort());
            ObjectOutputStream output_stream = new ObjectOutputStream(socket.getOutputStream());
            output_stream.writeObject(message);

        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to server socket", e);
        }

        try {

            Thread.sleep(50);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Message response;

        try {

            response = getResponse(socket);

        } catch (IOException e) {
            throw new RuntimeException("Failed to connect to input socket", e);
        }
        System.out.println("response: " + response.getMessageType());

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to close socket.", e);
        }

        return response;

    }

    private SSLSocket createSocket(InetAddress address, int port) throws IOException {

        SSLSocketFactory socket_factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) socket_factory.createSocket(address, port);

        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());

        return socket;
    }

    private Message getResponse(SSLSocket socket) throws IOException {

        ObjectInputStream input_stream = new ObjectInputStream(socket.getInputStream());

        try {
            return (Message) input_stream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }

    Message handleRequest(Message req) {

        Message msg = null;

        switch (req.getMessageType()) {

        case LOOKUP:
            msg = handleLOOKUPRequest(req);
            break;
        case PREDECESSOR:
            msg = handlePREDECESSORRequest(req);
            break;
        case NOTIFY:
            msg = handleNOTIFYRequest(req);
            break;
        case NODEKEY:
            msg = handleNODEKEYRequest(req);
            break;
        case PUT:
            msg = handlePUTRequest(req);
            break;
        case GET:
            msg = handleGETRequest(req);
            break;
        case DELETE:
            msg = handleDELETERequest(req);
            break;
        case RECLAIM:
            msg = handleRECLAIMRequest(req);
            break;
        }

        return msg;
    }

    private Message handleLOOKUPRequest(Message req) {

        System.out.println("Received LOOKUP");
        InetSocketAddress target = null;

        NodeKey key = (NodeKey) req.getArgument();

        target = this.peer.getChordProtocol().findSuccessor(key);
        return Message.createResponse(Message.Type.SUCCESSOR, target, req.getId());
    }

    private Message handlePREDECESSORRequest(Message req) {

        System.out.println("Received PREDECCESSOR");
        return Message.createResponse(Message.Type.PREDECESSOR, this.peer.getChordProtocol().getPredecessor(),
                req.getId());
    }

    private Message handleNOTIFYRequest(Message req) {
        System.out.println("Received NOTIFY");

        this.peer.getChordProtocol().notified(req.getSenderAddress());
        return Message.createResponse(Message.Type.OK, null, req.getId());
    }

    private Message handleNODEKEYRequest(Message req) {
        System.out.println("Received NODEKEY");
        return Message.createResponse(Message.Type.NODEKEY, this.peer.getNodeKey(), req.getId());
    }

    private Message handlePUTRequest(Message req) {

        System.out.println("Received PUT from " + req.getSenderAddress().getPort());

        Message<String> response;

        byte[] body = (byte[]) req.getData();
        InetSocketAddress originalPeer = req.getSenderAddress();

        String argument = (String) req.getArgument();
        String[] arguments = argument.split(",");

        String nodeKey = arguments[0];
        String fileID = arguments[1];
        int replicationDegree = Integer.parseInt(arguments[2]);
        int originalRepDegree = Integer.parseInt(arguments[3]);

        if (nodeKey.equals(peer.getNodeKey().toString())) { // if I'm the owner of the file

            InetSocketAddress nextPeer = peer.getChordProtocol().getSuccessor();
            response = Message.createRequestWithData(Message.Type.PUT,
                    nodeKey + "," + fileID + "," + replicationDegree + "," + originalRepDegree, originalPeer, body);
            sendMessage(nextPeer, response);
            return null;
        }

        if (peer.getFileManager().backupFile(body, fileID)) { // try to save file

            System.out.println("File Saved");
            peer.getFileManager().addOriginalPeers(fileID, originalPeer);
            peer.getFileManager().addFileRepDegree(fileID, originalRepDegree);

            if (replicationDegree == 1) {
                response = Message.createResponse(Message.Type.STORED, nodeKey + "," + fileID, req.getId());
                sendMessage(originalPeer, response);
                return null;

            } else {

                replicationDegree--;
                InetSocketAddress nextPeer = peer.getChordProtocol().getSuccessor();
                response = Message.createRequestWithData(Message.Type.PUT,
                        nodeKey + "," + fileID + "," + replicationDegree + "," + originalRepDegree, originalPeer, body);
                sendMessage(nextPeer, response);
                return null;
            }

        } else {

            response = Message.createResponse(Message.Type.KEEP, nodeKey + "," + fileID, req.getId());
            sendMessage(originalPeer, response);
            System.out.println("Capacity exceeded");
            return null;

        }
    }

    private Message handleGETRequest(Message req) {

        System.out.println("Received GET");

        Message<String> response = null;

        String argument = (String) req.getArgument();
        String[] arguments = argument.split(",");

        String nodeKey = arguments[0];
        String fileID = arguments[1];

        byte[] dataFile = peer.getFileManager().getDataFile(fileID);

        if (dataFile != null) {

        }

        else {

            InetSocketAddress nextPeer = peer.getChordProtocol().getSuccessor();
            response = Message.createRequestWithData(Message.Type.GET, nodeKey + "," + fileID, nextPeer, null);
            sendMessage(nextPeer, response);
            return null;

        }

        return null;
    }

    private Message handleDELETERequest(Message req) {

        System.out.println("Received DELETE");

        Message<String> response = null;

        String argument = (String) req.getArgument();
        String[] arguments = argument.split(",");

        String nodeKey = arguments[0];
        String fileID = arguments[1];

        if (!peer.getFileManager().deleteFile(fileID)) {

            System.out.println("No backup of this file");

        } else {
            System.out.println("File deleted");
            int replicationDegree = peer.getFileManager().getOriginalRepDegree(fileID);
            response = Message.createResponse(Message.Type.DECREASE, argument + "," + replicationDegree, req.getId());
            sendMessage(peer.getFileManager().getOriginalAddress(fileID), response);
            peer.getFileManager().deleteOriginalPeer(fileID);
        }

        return Message.createResponse(Message.Type.DELETE, null, req.getId());
    }

    private Message handleRECLAIMRequest(Message req) {

        System.out.println("Received RECLAIM");

        Message<String> response = null;

        Integer argument = (Integer) req.getArgument();
        // System.out.println("SAPC " + this.peer.getFileManager().getUsed_space());

        if (!peer.reclaimDiskSpace(argument))
            return Message.createResponse(Message.Type.ERROR, null, req.getId());
        else{
            // System.out.println("SAPCEEEEEEEEEEEE " + this.peer.getFileManager().getUsed_space());
            return Message.createResponse(Message.Type.OK, null, req.getId());
        }
    }

    void handleResponse(Message rsp) {

        // System.out.println("handle rsp type: " + rsp.getMessageType());

        switch (rsp.getMessageType()) {

        case SUCCESSOR:
            handleSuccessorResponse(rsp);
            break;
        case OK:
            System.out.println("Received OK from " + rsp.getSenderAddress().getHostName());
            break;
        case ERROR:
            System.out.println("Received ERROR from " + rsp.getSenderAddress().getHostName());
            break;
        case NODEKEY:
            handleNODEKEYResponse(rsp);
            break;
        case PREDECESSOR:
            handlePREDECESSORResponse(rsp);
            break;
        case DELETE:
            handleDELETEResponse(rsp);
            break;
        case STORED:
            handleSTOREDResponse(rsp);
            break;
        case DECREASE:
            handleDECREASEResponse(rsp);
            break;
        case KEEP:
            handleKEEPResponse(rsp);
            break;
        }
    }

    private void handleSuccessorResponse(Message rsp) {

        System.out.println("Received SUCCESSOR response");

        InetSocketAddress successor = null;

        try {

            successor = (InetSocketAddress) rsp.getArgument();

        } catch (ClassCastException e) {

            System.err.println("Failed to cast argument to InetSocketAddress." + e.getMessage());
        }

        this.peer.getChordProtocol().setIthFinger(0, successor);
    }

    private void handlePREDECESSORResponse(Message rsp) {

        System.out.println("Received PREDECESSOR response");

        try {

            if (rsp.getArgument() != null)
                System.out.println("PREDECESSOR CONFIRMED\n");

        } catch (ClassCastException e) {
            System.err.println("Failed to cast argument to InetSocketAddress." + e.getMessage());
        }
    }

    private void handleNODEKEYResponse(Message rsp) {
        System.out.println("Received NODEKEY response");

    }

    private void handleDELETEResponse(Message rsp) {
        System.out.println("Received DELETE response");
    }

    private void handleSTOREDResponse(Message rsp) {
        String argument = (String) rsp.getArgument();
        String[] arguments = argument.split(",");

        String fileID = arguments[1];
        System.out.println("STORED file " + fileID);
    }

    private void handleDECREASEResponse(Message rsp) {

        Message<String> response;

        String argument = (String) rsp.getArgument();
        String[] arguments = argument.split(",");
        String fileID = arguments[1];
        int replicationDegree = Integer.parseInt(arguments[2]);

        if (replicationDegree > 0) {

            replicationDegree--;
            peer.getFileManager().decreaseFileRepDegree(fileID);
            InetSocketAddress nextPeer = peer.getChordProtocol().getSuccessor();

            System.out.println("Received DECREASE replication degree of file " + fileID);

            response = Message.createResponse(Message.Type.DECREASE, argument + replicationDegree, rsp.getId());
            sendMessage(nextPeer, response);
        }

    }

    private void handleKEEPResponse(Message rsp) {

    String argument = (String) rsp.getArgument();
        String[] arguments = argument.split(",");
        String fileID = arguments[1];
        System.out.println("Unable to backup file " + fileID);
    }
}