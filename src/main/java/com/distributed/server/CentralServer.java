package com.distributed.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CentralServer extends UnicastRemoteObject implements CentralServerInterface {
  private String host;
  private int centralPort;
  private String centralName;
  private int[] serverPorts;
  private ServerLogger serverLogger;
  // 0 -> empty, 1 -> busy, 2 -> die
  private Map<Integer, Integer> serverStatus;

  //TODO check serverPorts.length > 1 in main
  public CentralServer(String host, int currPort, int[] serverPorts) throws RemoteException {
    this.host = host;
    this.centralPort = currPort;
    this.centralName = "CentralServer" + currPort;
    this.serverPorts = serverPorts;
    this.serverStatus = new HashMap();
    serverLogger = new ServerLogger();
    bindRMI();
    for (int port : this.serverPorts) {
      new Server(port, centralPort);
      this.serverStatus.put(port, 0);
    }
  }

  public static void main(String[] args) throws Exception {
    CentralServer centralServer = new CentralServer("127.0.0.1", 1200, new int[]{1300, 1400, 1500});
  }

  /**
   * Binds RMI
   */
  public void bindRMI() {
    try {
      Registry registry = LocateRegistry.createRegistry(centralPort);
      registry.rebind(centralName, this);
      serverLogger.log(centralName, centralName + " is running...");
    } catch (Exception e) {
      serverLogger.log(centralName, e.getMessage());
    }
  }

  @Override
  public int assignAliveServerToClient() {
    int serverChosen = generateRandomNumber(serverPorts.length);
    while (this.serverStatus.get(serverPorts[serverChosen]) != 0) {
      serverChosen = (serverChosen + 1) % serverPorts.length;
    }
    return serverPorts[serverChosen];
  }

  @Override
  public void killSlaveServer(int slaveServerPort) {
    try {
      Registry registry = LocateRegistry.getRegistry(slaveServerPort);
      ServerInterface stub = (ServerInterface) registry.lookup("Server" + slaveServerPort);
      stub.kill();
      serverStatus.put(slaveServerPort, 2);
    } catch (Exception e) {
      e.printStackTrace();
      serverLogger.log(centralName, e.getMessage());
    }
  }

  @Override
  public void restartSlaveServer(int slaveServerPort) throws RemoteException {
    new Server(slaveServerPort, centralPort);
    serverLogger.log(centralName, "restart " + slaveServerPort);
    for (int serverPort : serverPorts) {
      if (serverPort == slaveServerPort) continue;
      if (getServerStatus(serverPort) == 0) {
        try {
          Registry registry = LocateRegistry.getRegistry(serverPort);
          ServerInterface stub = (ServerInterface) registry.lookup("Server" + serverPort);
          stub.helpRecoverData(slaveServerPort);
          serverStatus.put(slaveServerPort, 0);
          break;
        } catch (Exception e) {
          e.printStackTrace();
          serverLogger.log(centralName, e.getMessage());
        }
      }
    }
    serverLogger.log(centralName, "No alive slave com.distributed.server found. Data recovery failed.");
  }

  @Override
  public int getServerStatus(int port) throws RemoteException {
    if (serverStatus.get(port) == null) return -1;
    return serverStatus.get(port);
  }

  @Override
  public void setServerStatus(int port, int status) throws RemoteException {
    serverStatus.put(port, status);
  }

  @Override
  public void receiveNotification(String message) throws RemoteException {
    serverLogger.log(centralName, message);
  }

  @Override
  public int[] getPeers(int toPort) throws RemoteException {
    int[] peers = new int[serverPorts.length - 1];
    int i = 0;
    for (int serverPort : serverPorts) {
      if (serverPort == toPort) continue;
      peers[i] = serverPort;
      i++;
    }
    return peers;
  }

  private int generateRandomNumber(int n) {
    Random random = new Random();
    return random.nextInt(n);
  }
}
