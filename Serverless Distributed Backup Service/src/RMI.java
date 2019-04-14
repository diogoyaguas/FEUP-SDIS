import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMI extends Remote {

    /**
     * Backup file service.
     *
     * @param filepath          the file path
     * @param replicationDegree the desired replication degree
     * @throws RemoteException
     */
    void backup(String filepath, int replicationDegree) throws RemoteException;

    /**
     * Restore file service.
     *
     * @param filepath the file path
     * @throws RemoteException
     */
    void restore(String filepath) throws RemoteException;

    /**
     * Delete file service.
     *
     * @param filepath the file path
     * @throws RemoteException
     */
    void delete(String filepath) throws RemoteException;

    /**
     * Reclaim space service.
     *
     * @param size new value for reserved peer storage space
     * @throws RemoteException
     */
    void reclaim(int size) throws RemoteException;

    /**
     * Retrieve state service.
     *
     * @throws RemoteException
     */
    void state() throws RemoteException;
}
