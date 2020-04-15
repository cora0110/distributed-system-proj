package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CentralServiceInterface extends Remote {

    void startServers(String[] serverAddresses);

    /**
     *
     * @return server address
     * @throws RemoteException
     */
    String assignServerToClient() throws RemoteException;

    void restartServer();

    void recoverData();


}
