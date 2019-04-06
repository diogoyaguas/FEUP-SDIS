package Peer;

import Channel.*;
import Service.RMI;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public class PeerProtocol {

    private static double protocolVersion;
    private static int serverId;
    private static String peerAp;

    private static InetAddress MCAddress, MDBAddress, MDRAddress;
    private static int MCPort, MDBPort, MDRPort;

    private static final AtomicInteger count = new AtomicInteger(0);
    private int peerID = 0;
    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;
    private static ScheduledThreadPoolExecutor exec;

    Peer(InetAddress MCAddress, int MCPort, InetAddress MDBAddress, int MDBPort, InetAddress MDRAddress, int MDRPort) {
        peerID = count.incrementAndGet();
        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);
        MC = new ChannelControl(MCAddress, MCPort);
        MDB = new ChannelBackup(MDBAddress, MDBPort);
        MDR = new ChannelRestore(MDRAddress, MDRPort);
    }

    public static void main(String[] args) {

        System.setProperty("java.net.preferIPv4Stack", "true");

        try {

            if (!initializeArgs(args)) return;

            Peer obj = new Peer();

            RMI stub = (RMI) UnicastRemoteObject.exportObject(obj, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind(peerAp, stub);

            System.err.println("Peer "+ peerAp + " created");

        } catch (Exception e) {
            System.err.println("\nPeer exception: " + e.toString());
            e.printStackTrace();
        }

        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);

    }

    private static boolean initializeArgs(String[] args) throws UnknownHostException {

        if (args.length != 3 && args.length != 9) {
            System.out.println("Usage:\tPeer <protocolVersion> <serverId> <peerApp> <MCAddress> <MCPort> <MDBAddress> <MDBPort> <MDRAddress> <MDRPort>\nUsage:\tPeer <protocolVersion> <serverId> <peerApp>");
            return false;
        } else if (args.length == 3) { //User didn't specify so we use default values

            MCAddress = InetAddress.getByName("224.0.0.0");
            MCPort = 8000;

            MDBAddress = InetAddress.getByName("224.0.0.0");
            MDBPort = 8001;

            MDRAddress = InetAddress.getByName("224.0.0.0");
            MDRPort = 8002;
        } else { //User specify values

            MCAddress = InetAddress.getByName(args[3]);
            MCPort = Integer.parseInt(args[4]);

            MDBAddress = InetAddress.getByName(args[5]);
            MDBPort = Integer.parseInt(args[6]);

            MDRAddress = InetAddress.getByName(args[7]);
            MDRPort = Integer.parseInt(args[8]);
        }

        protocolVersion = Double.parseDouble(args[0]);
        serverId = Integer.parseInt(args[1]);
        peerAp = args[2];

        printInfo();

        return true;
    }

    private static void printInfo() {

        System.out.println("\nVersion : " + protocolVersion + "\nServerId : " + serverId + "\nAccess Point : " + peerAp);
        System.out.println("\nMC: " + MCAddress + " | " + MCPort + "\nMDB: " + MDBAddress + " | " + MDBPort + "\nMDR: " + MDRAddress + " | " + MDRPort);
    }

}