package com.distributed.server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * CentralServer.java The central service that supports users logging on, adding or removing clients
 * or servers, and other housekeeping tasks. Assumption: the central server never fails.
 *
 * @version 2020-4-21
 */
public class CentralServer extends UnicastRemoteObject implements CentralServerInterface {
  private String host;
  private int centralPort;
  private String centralName;
  private int[] serverPorts;
  private ServerLogger serverLogger;
  // 0 -> empty, 1 -> busy, 2 -> die
  private Map<Integer, Integer> serverStatus;

  private static final int DEFAULT_CENTRAL_PORT = 1200;
  private static final int[] DEFAULT_SERVER_PORTS = new int[]{1300, 1400, 1500, 1600, 1700};


  /**
   * Constructor
   *
   * @param host        ip
   * @param currPort    port# for central server
   * @param serverPorts port# for servers
   * @throws RemoteException
   */
  public CentralServer(String host, int currPort, int[] serverPorts) throws RemoteException {
    this.host = host;
    this.centralPort = currPort;
    this.centralName = "CentralServer" + currPort;
    this.serverPorts = serverPorts;
    this.serverStatus = new HashMap();
    serverLogger = new ServerLogger();
    bindRMI();
    for (int port : this.serverPorts) {
      Server server = new Server(port, centralPort);
      ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
      Registry registry = LocateRegistry.createRegistry(port);
      registry.rebind(Server.class.getSimpleName() + port, stub);
      serverLogger.log("Server" + port + " is running...");
      this.serverStatus.put(port, 0);
    }
  }

  public static void main(String[] args) throws Exception {
    Logger rmiioLogger = Logger.getLogger("com.healthmarketscience.rmiio");
    rmiioLogger.setLevel(Level.SEVERE);
    if (args.length < 2) {
      System.err.println("Not specify at least one central port and one server port.");
      System.out.println("Using default central port: " + DEFAULT_CENTRAL_PORT + ". Server ports: " + Arrays.toString(DEFAULT_SERVER_PORTS));
      CentralServer centralServer = new CentralServer("127.0.0.1", DEFAULT_CENTRAL_PORT, DEFAULT_SERVER_PORTS);
      return;
    } else {
      List<Integer> ports = Arrays.stream(args).map(Integer::parseInt).collect(Collectors.toList());
      List<Integer> serverPortsList = ports.subList(1, ports.size());
      int[] serverPorts = serverPortsList.stream().mapToInt(i -> i).toArray();
      CentralServer centralServer = new CentralServer("127.0.0.1", ports.get(0), serverPorts);
    }
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

  /**
   * Randomly assign an alive server to client.
   */
  @Override
  public int assignAliveServerToClient() {
    int serverChosen = generateRandomNumber(serverPorts.length);
    while (this.serverStatus.get(serverPorts[serverChosen]) != 0) {
      serverChosen = (serverChosen + 1) % serverPorts.length;
    }
    return serverPorts[serverChosen];
  }

  /**
   * Kill a slave server and update serverStatus
   *
   * @param slaveServerPort port
   */
  @Override
  public void killSlaveServer(int slaveServerPort) {
    try {
      Registry registry = LocateRegistry.getRegistry(slaveServerPort);
      registry.unbind(Server.class.getSimpleName() + slaveServerPort);
      serverStatus.put(slaveServerPort, 2);
    } catch (Exception e) {
      e.printStackTrace();
      serverLogger.log(centralName, e.getMessage());
    }
  }

  /**
   * Restart a slave server: assign a live server as helper to help the server restart.
   *
   * @param slaveServerPort port# for the restarting server
   * @throws RemoteException
   */
  @Override
  public void restartSlaveServer(int slaveServerPort) throws RemoteException {
    if (serverStatus.get(slaveServerPort) != 2) return;
    Server server = new Server(slaveServerPort, centralPort);
    ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
    Registry registry = LocateRegistry.getRegistry(slaveServerPort);
    registry.rebind(Server.class.getSimpleName() + slaveServerPort, stub);

    for (int serverPort : serverPorts) {
      if (serverPort == slaveServerPort) continue;
      if (getServerStatus(serverPort) == 0) {
        try {
          serverLogger.log(centralName, "Assign Server" + serverPort +
                  " to help Server " + slaveServerPort + " recover data.");
          registry = LocateRegistry.getRegistry(serverPort);
          ServerInterface aliveServer = (ServerInterface) registry.lookup("Server" + serverPort);
          aliveServer.helpRecoverData(slaveServerPort);
          serverStatus.put(slaveServerPort, 0);
          return;
        } catch (Exception e) {
          e.printStackTrace();
          serverLogger.log(centralName, e.getMessage());
          return;
        }
      }
    }
    serverLogger.log(centralName, "No alive slave server found. Data recovery failed.");
  }

  /**
   * Get the server status
   *
   * @param port server port #
   * @return 0 -> empty, 1 -> busy, 2 -> dead, -1 -> Not found
   * @throws RemoteException
   */
  @Override
  public int getServerStatus(int port) throws RemoteException {
    if (serverStatus.get(port) == null) return -1;
    return serverStatus.get(port);
  }

  /**
   * Set server status as input
   *
   * @param port   server port #
   * @param status 0 -> empty, 1 -> busy, 2 -> dead
   * @throws RemoteException
   */
  @Override
  public void setServerStatus(int port, int status) throws RemoteException {
    serverStatus.put(port, status);
  }

  /**
   * Receive notification from servers
   *
   * @param message msg from servers
   * @throws RemoteException
   */
  @Override
  public void receiveNotification(String message) throws RemoteException {
    serverLogger.log(centralName, message);
  }


  /**
   * Get all peers (regardless of the server status) to the server from toPort
   *
   * @param toPort input port number
   * @return list of peer port numbers
   * @throws RemoteException
   */
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

  /**
   * generate a random number from 0 to n
   *
   * @param n
   * @return a random number from 0 to n
   */
  private int generateRandomNumber(int n) {
    Random random = new Random();
    return random.nextInt(n);
  }
}
