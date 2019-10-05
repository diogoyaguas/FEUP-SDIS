package comms;

import program.Peer;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Receiver extends Thread {

    private Peer peer;
    private Dispatcher dispatcher;
    private ExecutorService thread_pool;
    private SSLServerSocket server_socket;

    public Receiver(Peer peer, Dispatcher dispatcher) {

        this.peer = peer;
        this.dispatcher = dispatcher;
        this.thread_pool = Executors.newFixedThreadPool(5);

        int port = peer.getAddress().getPort();

        SSLServerSocketFactory server_factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        try {

            this.server_socket = (SSLServerSocket) server_factory.createServerSocket(port);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open port " + port);
            return;
        }

        server_socket.setNeedClientAuth(true);
        server_socket.setEnabledCipherSuites(server_socket.getSupportedCipherSuites());

    }

    @Override
    public void run() {
        while (true) {
            SSLSocket socket;

            try {
                socket = (SSLSocket) this.server_socket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            thread_pool.submit(() -> processConnection(socket));
        }
    }

    private void processConnection(SSLSocket socket) {

        ObjectOutputStream output;
        ObjectInputStream input;

        try {
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

        } catch (IOException e) {
            throw new RuntimeException("Failed to create output/input streams on socket connection", e);
        }

        Message message;

        try {

            message = (Message) input.readObject();

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class not found", e);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read object from stream", e);
        }

        if(message.getSenderAddress().equals(this.peer.getAddress()))
            return;
            
        Message response = null;

        if (message.isRequest())
            response = dispatcher.handleRequest(message);
        else
            dispatcher.handleResponse(message);

        try {

            if (response != null) {
                output.writeObject(response);
            }

            output.close();
            input.close();

        } catch (IOException e) {
            throw new RuntimeException("Failed to write object to stream", e);
        }
    }
}