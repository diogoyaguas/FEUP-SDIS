import java.rmi.Remote;
import java.rmi.RemoteException;

public interface remoteInterface extends Remote {

    int register(String plate, String owner) throws RemoteException;

    String lookup(String plate) throws RemoteException;

}