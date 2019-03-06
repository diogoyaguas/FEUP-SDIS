import java.io.*;
import java.net.*;
import java.util.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

class server implements remoteInterface {

    private static String rmObjectName;
    private static Hashtable<String, String> cars;

    public static void main(String args[]) throws IOException, RemoteException {

        if (args.length != 1) {

            System.out.println("Usage:\tjava Server <remote_object_name>");
            return;
        } 
        
        else {

            rmObjectName = args[0];
        }

        try {
            server obj = new server();
            remoteInterface stub = (remoteInterface) UnicastRemoteObject.exportObject(obj, 0);

            Registry registry = LocateRegistry.getRegistry();
            registry.bind(rmObjectName, stub);

            cars = new Hashtable<String, String>();

            System.err.println("Server ready");
        }
    }

    @Override 
    public int register(String plate, String onwer) throws RemoteException {

        int request;

        if(cars.containsKey(plate)) {

            request = -1;
        }

        else {

            cars.put(plate, owner);
            request = cars.size();
        }

            System.out.println("register " + plate + " " + owner + " :: " + request);

		return request;
    }

    @Override public String lookup(String plate)  throws RemoteException {

        String owner = cars.containsKey(plate) ? cars.get(plate) : "ERROR";

		System.out.println("lookup " + plate + " :: " + owner);

		return owner;
    }

}