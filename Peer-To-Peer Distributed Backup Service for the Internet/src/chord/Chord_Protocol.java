package chord;

import java.net.InetSocketAddress;
import java.io.Serializable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceArray;

import comms.Dispatcher;
import comms.Message;
import program.NodeKey;
import program.Peer;

public class Chord_Protocol extends Thread {

    private Peer peer;
    private Dispatcher dispatcher;
    private InetSocketAddress predecessor;
    private AtomicReferenceArray<InetSocketAddress> finger_table;
    private ScheduledThreadPoolExecutor thread_exec = new ScheduledThreadPoolExecutor(4);
    private static final int M = 7;

    public Chord_Protocol(InetSocketAddress socket_address, Peer peer, Dispatcher dispatcher) {

        finger_table = new AtomicReferenceArray<>(M);
        this.peer = peer;
        this.dispatcher = dispatcher;
        this.predecessor = null;
    }

    public void create() {
        System.out.println("CREATE\n");
        this.setIthFinger(0, peer.getAddress());
    }

    public void join(InetSocketAddress socket_address) {
        System.out.println("JOIN\n");

        Message<NodeKey> message = Message.createRequest(Message.Type.LOOKUP, peer.getNodeKey(), socket_address);
        InetSocketAddress successor_address = dispatcher.requestAddress(socket_address, message);

        this.setIthFinger(0, successor_address);

    }

    @Override
    public void run() {

        Check_Predecessor checkPredecessorThread = new Check_Predecessor(this, dispatcher);
        thread_exec.scheduleAtFixedRate(checkPredecessorThread, 10, 10000, TimeUnit.MILLISECONDS);

        Finger_Table finger_table = new Finger_Table(this);
        thread_exec.scheduleAtFixedRate(finger_table, 2000, 5000, TimeUnit.MILLISECONDS);

        Stabilize stabilize_thread = new Stabilize(this);
        thread_exec.scheduleAtFixedRate(stabilize_thread, 10, 2500, TimeUnit.MILLISECONDS);

    }

    void notify(InetSocketAddress successor) {

        if (successor.equals(this.peer.getAddress())) {
            System.out.println("Notifying self, ignoring");
            return;
        }

        System.out.println("notifying node");

        Message<Serializable> message = Message.createRequest(Message.Type.NOTIFY, peer.getAddress(),
                peer.getAddress());
        Message response = dispatcher.sendMessage(successor, message);

        if (response.getMessageType() != Message.Type.OK) {
            System.err.println("Predecessor guess is wrong");
        }

        System.out.println("Notification received");
    }

    public void notified(InetSocketAddress new_predecessor) {

        if (this.predecessor == null) {
            this.predecessor = new_predecessor;
            return;
        }

        NodeKey new_pre = NodeKey.keyFromAddress(new_predecessor);
        NodeKey curr_pre = NodeKey.keyFromAddress(this.predecessor);
        if (new_pre.isBetween(curr_pre, this.peer.getNodeKey()))
            this.predecessor = new_predecessor;
    }

    public InetSocketAddress findSuccessor(NodeKey node_key) {
        System.out.println("Finding Successor of key " + node_key);

        InetSocketAddress curr_successor = this.getSuccessor();

        if (node_key.isBetween(this.peer.getNodeKey(), NodeKey.keyFromAddress(curr_successor)))
            return curr_successor;

        InetSocketAddress pred_finger = findClosestPrecedingFinger(node_key);

        if (pred_finger.equals(this.peer.getAddress())) {
            return pred_finger;
        }

        Message<NodeKey> request = Message.createRequest(Message.Type.LOOKUP, node_key, this.peer.getAddress());
        return dispatcher.requestAddress(pred_finger, request);

    }

    private InetSocketAddress findClosestPrecedingFinger(NodeKey node_key) {

        for (int i = finger_table.length() - 1; i > 0; i--) {
            InetSocketAddress finger_entry = finger_table.get(i);
            if (finger_entry != null
                    && NodeKey.keyFromAddress(finger_entry).isBetween(this.peer.getNodeKey(), node_key))
                return finger_entry;
        }
        return this.peer.getAddress();

    }

    /**
     * @return m
     */
    static int get_M() {
        return M;
    }

    /**
     * @return finger_table
     */
    AtomicReferenceArray<InetSocketAddress> getFingerTable() {
        return this.finger_table;
    }

    public InetSocketAddress getSuccessor() {
        return this.finger_table.get(0);
    }

    public InetSocketAddress getPredecessor() {
        return this.predecessor;
    }

    public Peer getPeer() {
        return this.peer;
    }

    public Dispatcher getDispatcher() {
        return this.dispatcher;
    }

    public InetSocketAddress get_successor(int idx) {
        return this.finger_table.get(idx);
    }

    public void set_successor(int idx, InetSocketAddress successor) {
        this.finger_table.set(idx, successor);
    }

    public ScheduledThreadPoolExecutor getThread() {

        return thread_exec;
    }

    /**
     * SETS
     */

    public void setIthFinger(int index, InetSocketAddress node) {
        if (index == 0)
            this.notify(node);
        this.finger_table.set(index, node);
    }

    void setPredecessor(InetSocketAddress pre) {
        this.predecessor = pre;
    }

}