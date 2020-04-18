package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CentralServerInterface extends Remote {

  int assignAliveServerToClient() throws RemoteException;

  void killSlaveServer(int slaveServerPort) throws RemoteException;

  void restartSlaveServer(int slaveServerPort) throws RemoteException;


}
