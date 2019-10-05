package program;

import chord.Chord_Protocol;
import comms.Dispatcher;
import comms.Receiver;
import storage.FileManager;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Peer {

    private InetSocketAddress peer_address;
    private NodeKey peer_key;
    private Chord_Protocol chord_protocol;
    private Receiver receiver;
    private Dispatcher dispatcher;
    private FileManager file_system;
    private ExecutorService executor_service;

    Peer(InetSocketAddress address) {

        this.peer_address = address;
        this.peer_key = NodeKey.keyFromAddress(address);
        this.dispatcher = new Dispatcher(this);
        this.file_system = new FileManager(peer_key.toString());
        this.executor_service = Executors.newFixedThreadPool(5);

        setUpThreadWorkers();
    }

    private void setUpThreadWorkers() {
        this.receiver = new Receiver(this, this.dispatcher);
        this.chord_protocol = new Chord_Protocol(peer_address, this, this.dispatcher);
    }

    private void initThreadWorkers() {
        receiver.start();
        chord_protocol.start();
    }

    void create() {
        initThreadWorkers();
        chord_protocol.create();
    }

    void join(InetSocketAddress address) {
        initThreadWorkers();
        chord_protocol.join(address);
    }

    public boolean reclaimDiskSpace(long wanted_space) {
        long wanted_bytes = wanted_space * 1000;

        while (this.file_system.getUsed_space() > wanted_bytes) {
            String file_name = this.file_system.getMostSatisfiedFile();
            // System.out.println("-------------USED SPACE: " + this.file_system.getUsed_space());
            // System.out.println("-------------VALUE: " + file_name);

            if (file_name == null) {
                System.out.println("Nothing more to delete");
                return this.file_system.getUsed_space() < wanted_space;
            }

            this.file_system.deleteFile(file_name);

        }

        this.file_system.setMax_space(wanted_bytes);
        return true;
    }





    public InetSocketAddress getAddress() {
        return this.peer_address;
    }

    public NodeKey getNodeKey() {
        return this.peer_key;
    }

    public Chord_Protocol getChordProtocol() {
        return this.chord_protocol;
    }

    public FileManager getFileManager() {
        return this.file_system;
    }
}