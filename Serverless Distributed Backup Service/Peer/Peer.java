package Peer;

import Service.RMI;

import java.util.concurrent.atomic.AtomicInteger;

public class Peer implements RMI {

    private static final AtomicInteger count = new AtomicInteger(0);
    private int peerID = 0;


    Peer() {
        this.peerID = count.incrementAndGet();
    }

    public int getId() {
        return this.peerID;
    }

    public synchronized void backup(String filepath, int replicationDegree) {

    }

    public void restore(String filepath) {

    }

    public void delete(String filepath) {

    }
}