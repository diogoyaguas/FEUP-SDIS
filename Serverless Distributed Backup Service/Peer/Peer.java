import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Peer {

    private static double protocolVersion;
    private static int serverId;
    private static String peerAp;

    InetAddress MCAddress, MDBAddress, MDRAddress;
    int MCPort, MDBPort, MDRPort;

    private static ChannelControl MC;
    private static ChannelBackup MDB;
    private static ChannelRestore MDR;

    private static MulticastSocket socket;

    private static ScheduledThreadPoolExecutor exec;
    private static Storage storage;

    public static void main(String args[]) throws ClassNotFoundException, IOException {

        //CHECK & INITIALIZE ARGS
        if(!initializeArgs(String args[]))
            return;

        try {

            Peer peer = new Peer();
            RMIRemote stub = (RMIRemote) UnicastRemoteObject.exportObject(peer, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind(peerAp, stub);

            System.err.println("\nPeer configured\n");
    
        } catch (Exception e) {
            System.err.println("\nPeer exception: " + e.toString());
            e.printStackTrace();
        }

        startMulticast();
    }

    private static boolean initializeArgs(String[] args) throws UnknownHostException { 

        if(args.length != 3 && args.length != 9){
            System.out.println("Usage:\tPeer <protocolVersion> <serverId> <peerApp> <MCAddress> <MCPort> <MDBAddress> <MDBPort> <MDRAddress> <MDRPort>");
            return false;
        }

        else if(args.length == 3){

            //User didn't specify so we use default values
            MCAddress = InetAddress.getByName("224.0.0.0");
            MCPort = 8000;

            MDBAddress = InetAddress.getByName("224.0.0.0");
            MDBPort = 8001;

            MDRAddress = InetAddress.getByName("224.0.0.0");
            MDRPort = 8002;
        }

        else {

            MCAddress = InetAddress.getByName(args[3]);
            MCPort = Integer.parseInt(args[4]);

            MDBAddress = InetAddress.getByName(args[5]);
            MDBPort =  Integer.parseInt(args[6]);

            MDRAddress = InetAddress.getByName(args[7]);
            MDRPort = Integer.parseInt(args[8]);
        }

        protocolVersion = Double.parseDouble(args[0]);
        serverId =Integer.parseInt(args[1]);
        peerAp = args[2];

        MC = new ChannelControl(MCAddress, MCPort);
        MDB = new ChannelBackup(MDBAddress, MDBPort);
        MDR = new ChannelRestore(MDRAddress, MDRPort);

        printInfo();

        return true;
    }

    private static void printInfo() {

        System.out.println();
        System.out.println("version : " + protocolVersion + "serverId : " + serverId + "access point : " + peerAp);
        System.out.println();
        System.out.println("MC  : " + MCAddress + ":" + MCPort + "MDB : " + MDBAddress + ":" + MDBPort + "MDR : " + MDRAddress + ":" + MDRPort);
        System.out.println();
    }

    //GETS
    public static ChannelBackup getMDB() { return MDB; }
    public static ChannelRestore getMDR() { return MDR; }
    public static ChannelControl getMC() { return MC; }
    public static int getServerID() { return serverId; }
    public static ScheduledThreadPoolExecutor getExec() { return exec; }

    //START MULTICAST
    private static void startMulticast(){

        exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(250);
        exec.execute(MC);
        exec.execute(MDB);
        exec.execute(MDR);
    }

    //ACTIONS

}

/*
      System.setProperty("java.net.preferIPv4Stack", "true");

         deserializeStorage(); // loads storage

      

        Runtime.getRuntime().addShutdownHook(new Thread(Peer::serializeStorage)); // Saves storage if CTRL-C is pressed

*/