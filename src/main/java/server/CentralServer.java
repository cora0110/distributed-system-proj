package server;

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
  private Map<Integer, Boolean> serverStatus; // port -> status

  public CentralServer(String host, int currPort, int[] serverPorts) throws RemoteException {
    this.host = host;
    this.centralPort = currPort;
    this.centralName = "CentralServer" + currPort;
    this.serverPorts = serverPorts;
    this.serverStatus = new HashMap();
    for (int port : this.serverPorts) {
      new Server(port);
      this.serverStatus.put(port, true);
    }
    serverLogger = new ServerLogger();
    bindRMI();
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public int getCentralPort() {
    return centralPort;
  }

  public void setCentralPort(int centralPort) {
    this.centralPort = centralPort;
  }

  public int[] getServerPorts() {
    return serverPorts;
  }

  public void setServerPorts(int[] serverPorts) {
    this.serverPorts = serverPorts;
  }

  /**
   * Binds RMI
   */
  public void bindRMI() {
    try {
      Registry registry = LocateRegistry.createRegistry(centralPort);
      registry.rebind(centralName, this);
      serverLogger.log(centralName + " is running...");
    } catch (Exception e) {
      serverLogger.log(e.getMessage());
    }
  }

  @Override
  public int assignAliveServerToClient() {
    int serverChosen = generateRandomNumber(serverPorts.length);
    while (!this.serverStatus.get(serverPorts[serverChosen])) {
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
      serverStatus.put(slaveServerPort, false);
    } catch (Exception e) {
      serverLogger.log("Failed Restart Slave Server! Exception: " + e.getMessage());
    }
  }

  @Override
  public void restartSlaveServer(int slaveServerPort) throws RemoteException {
    new Server(slaveServerPort);
    for (int serverPort : serverPorts) {
      if (serverPort == slaveServerPort) continue;
      try {
        Registry registry = LocateRegistry.getRegistry(serverPort);
        ServerInterface stub = (ServerInterface) registry.lookup("Server" + serverPort);

        stub.helpRestartServer(slaveServerPort);
        serverStatus.put(slaveServerPort, true);
        break;
      } catch (Exception e) {
        serverLogger.log("Failed Restart Slave Server! Exception: " + e.getMessage());
      }
    }
    serverLogger.log("No alive slave server found!");
  }

  /**
   * Randomly get a number in range 0 and n - 1
   *
   * @param n int
   * @return int
   */
  private int generateRandomNumber(int n) {
    Random random = new Random();
    return random.nextInt(n);
  }

  public static void main(String[] args) throws Exception {
    CentralServer centralServer = new CentralServer("127.0.0.1", 12345, new int[]{12341, 12342});
  }
}
