package com.distributed.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** CentralServerInterface.java
 * Interface of class CentralServer
 *
 * @version 2020-4-21
 */
public interface CentralServerInterface extends Remote {

  int assignAliveServerToClient() throws RemoteException;

  void killSlaveServer(int slaveServerPort) throws RemoteException;

  void restartSlaveServer(int slaveServerPort) throws RemoteException;

  int getServerStatus(int port) throws RemoteException;

  void setServerStatus(int port, int status) throws RemoteException;

  void receiveNotification(String message) throws RemoteException;

  int[] getPeers(int toPort) throws RemoteException;
}
