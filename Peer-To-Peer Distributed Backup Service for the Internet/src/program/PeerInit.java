package program;

import comms.RMI;
import protocols.DeleteFile;
import protocols.GetFile;
import protocols.PutFile;
import protocols.ReclaimSpace;
import storage.FileManager;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

public class PeerInit implements RMI {

    private static Peer peer;

    public static void main(String[] args) {

        if (args.length != 1 && args.length != 3) {
            System.err.println(
                    "\n Usage:\tPeer <port_number>\n\tPeer <port_number> <network_address> <port_numer_peer1>");
            System.exit(1);
        }

        setProperties();

        InetAddress local_add = null;

        try {

            local_add = InetAddress.getLocalHost();

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        InetSocketAddress socket_add = new InetSocketAddress(local_add, Integer.parseInt(args[0]));

        peer = new Peer(socket_add);
        PeerInit obj = new PeerInit();
        RMI stub;

        try {

            stub = (RMI) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry("localhost");
            registry.rebind(peer.getNodeKey().toString(), stub);

        } catch (RemoteException error) {
            error.printStackTrace();
        }

        if (args.length == 1) {

            peer.create();

        } else {

            InetAddress ip = null;

            try {

                ip = InetAddress.getByName(args[1]);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            if (ip == null) {

                System.err.println("Could not retrive ip with address: " + args[1]);
                return;
            }

            InetSocketAddress address = new InetSocketAddress(ip, Integer.parseInt(args[2]));
            peer.join(address);
        }
    }

    private static void setProperties() {
        System.setProperty("javax.net.ssl.keyStore", "../keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "mortelenta");
        System.setProperty("javax.net.ssl.trustStore", "../truststore");
        System.setProperty("javax.net.ssl.trustStorePassword", "mortelenta");
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    @Override
    public void backup(String filepath, int replicationDegree) {

        String fileID;
        File tempFile = new File(filepath);
        byte[] file;

        try {

            fileID = FileManager.getFile_ID(tempFile);
            file = FileManager.loadFile_bytes(tempFile);
            peer.getFileManager().addFileRepDegree(fileID, replicationDegree);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        byte[] body = Arrays.copyOf(file, file.length);
        PutFile th = new PutFile(fileID, body, replicationDegree, peer.getChordProtocol());
        peer.getChordProtocol().getThread().execute(th);
    }

    @Override
    public void restore(String filepath) {

        String fileID;
        File tempFile = new File("../TestFiles/" + filepath);

        fileID = FileManager.getFile_ID(tempFile);

        GetFile th = new GetFile(fileID, filepath, peer.getChordProtocol());
        peer.getChordProtocol().getThread().execute(th);

    }

    @Override
    public void delete(String filepath) {

        String fileID;
        File tempFile = new File(filepath);

        fileID = FileManager.getFile_ID(tempFile);

        DeleteFile th = new DeleteFile(fileID, peer.getChordProtocol());
        peer.getChordProtocol().getThread().execute(th);
    }

    @Override
    public void reclaim(int size) {

        if(size < 0)
            return;

        ReclaimSpace th = new ReclaimSpace(size, peer.getChordProtocol());
        peer.getChordProtocol().getThread().execute(th);

    }
}