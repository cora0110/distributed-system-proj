package server;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamWrapper;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import model.AcceptAck;
import model.CommitParams;
import model.Document;
import model.PrepareAck;
import model.Request;
import model.Result;
import model.Section;
import model.User;
import notification.NotiServerRunnable;

public class Server extends UnicastRemoteObject implements ServerInterface {
  public int port;
  public String serverName;
  private ServerLog serverLog;
  private DocumentDatabase documentDatabase;
  private AliveUserDatabase aliveUserDatabase;
  private UserDatabase userDatabase;
  private ReentrantReadWriteLock readWriteLock;

  private NotiServerRunnable notificationThread;

  private final String DATA_DIR = "./server_data" + port + "/";

  public Server(int port) throws RemoteException {
    this.port = port;
    this.serverName = "Server" + port;
    serverLog = new ServerLog();
    userDatabase = initUserDB();
    documentDatabase = initDocumentDB();
    aliveUserDatabase = new AliveUserDatabase();
    bindRMI();
  }

  /**
   * Binds RMI
   */
  public void bindRMI() {
    try {
      Registry registry = LocateRegistry.createRegistry(port);
      registry.rebind(serverName, this);
      serverLog.log(serverName + " is running...");
    } catch (Exception e) {
      serverLog.log(e.getMessage());
    }
  }

  @Override
  public Result prepare(UUID transactionID) throws RemoteException {
    return null;
  }

  @Override
  public PrepareAck participantsPrepare(UUID transactionID) {
    return null;
  }

  @Override
  public Result accept(UUID transactionID) throws RemoteException {
    return null;
  }

  @Override
  public AcceptAck participantsAccept(UUID transactionID, CommitParams commitParams) {
    return null;
  }

  @Override
  public Result commit(UUID transactionID, CommitParams commitParams) throws RemoteException {
    return null;
  }

  @Override
  public Result initDocument(String docName, User user) throws RemoteException {
    return null;
  }

  @Override
  public Result initSection(int sectionNum, User user) throws RemoteException {
    return null;
  }

  @Override
  public Result createUser(User user) throws RemoteException {
    String username = user.getUsername();
    if (userDatabase.isUsernameAvailable(username)) {
      CommitParams commitParams =
              new CommitParams(user, 1, null, null, -1, 0);
      Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);
      if (result.getStatus() == 1) return new Result(1, "Create user succeed");
      else return new Result(0, "Request aborted.");
    } else {
      return new Result(0, "Username already exists");
    }
  }

  @Override
  public Result login(User user) throws RemoteException {
    if (aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Already logged in.");
    } else {
      // check username and password
      User loggedInUser = userDatabase.doLogin(user.getUsername(), user.getPassword());
      if (loggedInUser != null) {

        CommitParams commitParams =
                new CommitParams(user, 1, null, null, -1, 1);

        // TODO: 2PC AND RETURN TOKEN
        Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);
        if (result.getStatus() == 0) {
          return new Result(0, "Request aborted.");
        }
        String token = aliveUserDatabase.getTokenbyUser(user);

        if (token != null) {
          // TODO: NITIFICATION THREAD
//          notificationThread = new NotiServerRunnable(user, socket.getInetAddress().getHostName(), (Integer) args[2]);
//          notificationThread.run();
          System.out.println("New user logged in: " + user.getUsername());
          return new Result(1, token);
        } else {
          return new Result(0, "Token generation failure while logging in.");
        }
      } else {
        return new Result(0, "Username and password do not match.");
      }
    }
  }

  @Override
  public Result logout(User user) throws RemoteException {
    // TODO: NOTIFICATION THREAD
    notificationThread.stop();

    CommitParams commitParams =
            new CommitParams(user, 0, null, null, -1, 1);
    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);

    if (result.getStatus() == 1) {
      System.out.println("User logged out: " + user.getUsername());
      return new Result(1, "succeed");
    } else {
      return new Result(0, "Request aborted.");
    }

  }

  //TODO: CDA MANAGER NOT ADDED HERE
  @Override
  public Result edit(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.equals(aliveUserDatabase.getUserByToken(request.getToken()))) {
      return new Result(0, "User does not match token.");
    }

    Document document = documentDatabase.getDocumentByName(request.getDocName());
    if (document == null) {
      return new Result(0, "Document does not exist.");
    }

    if (!document.hasPermit(user)) {
      return new Result(0, "You do not have access.");
    }

    Section section = document.getSection(request.getSectionNum());
    if (section == null) {
      return new Result(0, "Section does not exist.");
    }

    User editingUser = section.getOccupant();
    if (editingUser != null) {
      return new Result(0, "The section is being edited");
    }

    // set occupant to that section
    CommitParams commitParams = new CommitParams(user, 2.1, null,
            request.getDocName(), request.getSectionNum(), 2);
    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);

    if (result.getStatus() == 0) {
      return new Result(0, "Request aborted.");
    }

    try {
      InputStream inputStream = new FileInputStream(section.getPath());
      SimpleRemoteInputStream remoteInputStream = new SimpleRemoteInputStream(inputStream);
      return new Result(1, "Succeed", remoteInputStream);
    } catch (IOException ioe) {
      return new Result(0, "IO Exception while accessing the section");
    }
  }

  @Override
  public Result editEnd(User user, FileInputStream fileInputStream) throws RemoteException {
      return null;
  }

  @Override
  public Result createDocument(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.equals(aliveUserDatabase.getUserByToken(request.getToken()))) {
      return new Result(0, "User does not match token.");
    }

    Document document = documentDatabase.getDocumentByName(request.getDocName());
    if (document != null) {
      return new Result(0, "Document already exists.");
    }

    CommitParams commitParams = new CommitParams(user, 1, null,
            request.getDocName(), request.getSectionNum(), 2);
    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);
    if (result.getStatus() == 1) {
      return new Result(1, "Succeed");
    } else {
      return new Result(0, "Request aborted.");
    }
  }

  @Override
  public Result showSection(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.equals(aliveUserDatabase.getUserByToken(request.getToken()))) {
      return new Result(0, "User does not match token.");
    }

    Document document = documentDatabase.getDocumentByName(request.getDocName());
    if (document == null) {
      return new Result(0, "Document does not exist.");
    }

        if (!document.hasPermit(user)) {
            return new Result(0, "You do not have access.");
        }

    Section section = document.getSection(request.getSectionNum());
    if (section == null) {
      return new Result(0, "Section does not exist.");

    }

        User editingUser = section.getOccupant();
        if (editingUser == null) {
            return new Result(1, "None");
        }
        return new Result(1, editingUser.getUsername());
    }

  @Override
  public Result showDocumentContent(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.equals(aliveUserDatabase.getUserByToken(request.getToken()))) {
      return new Result(0, "User does not match token.");
    }

    Document document = documentDatabase.getDocumentByName(request.getDocName());
    if (document == null) {
      return new Result(0, "Document does not exist.");
    }

    Vector<InputStream> streamVector = new Vector<>();

    try {
      for (Section s : document.getSection()) {
        streamVector.add(new FileInputStream(s.getPath()));
      }
      SequenceInputStream sequenceInputStream = new SequenceInputStream(streamVector.elements());
      RemoteInputStream remoteInputStream = new SimpleRemoteInputStream(sequenceInputStream);
      return new Result(1, "Succeed", remoteInputStream);
    } catch (IOException ioe) {
      return new Result(0, "Failure accessing section.");
    }
  }


  @Override
  public Result listOwnedDocs(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.equals(aliveUserDatabase.getUserByToken(request.getToken()))) {
      return new Result(0, "User does not match token.");
    }

    String[] docs = documentDatabase.getAllDocumentsNames(user);

    if (docs.length == 0) {
      return new Result(1, "None");
    }

    String names = String.join(",", docs);
    return new Result(1, names);
  }

  @Override
  public Result shareDoc(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.equals(aliveUserDatabase.getUserByToken(request.getToken()))) {
      return new Result(0, "User does not match token.");
    }

    Document document = documentDatabase.getDocumentByName(request.getDocName());
    if (document == null) {
      return new Result(0, "Document does not exist.");
    }

    if (!document.getCreator().equal(user)) {
        return new Result(0, "You do not have access.");
    }

    CommitParams commitParams = new CommitParams(request.getTargetUser(), 2.2,
            null, request.getDocName(), -1, 2);
    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);
    if (result.getStatus() == 1) {
      // TODO: 4/17/20 assign to other servers, shutdown hook
      return new Result(1, "Succeed");
    } else {
      return new Result(0, "Request aborted");
    }
  }

  @Override
  public void kill() throws RemoteException {
    Registry registry = LocateRegistry.getRegistry(this.port);
    try {
      registry.unbind(this.serverName);
    } catch (NotBoundException e) {
      e.printStackTrace();
    }

  }

  //TODO: ISSUES HERE
  @Override
  public boolean restart(DocumentDatabase documentDatabase,
                         AliveUserDatabase aliveUserDatabase,
                         UserDatabase userDatabase) {
    this.documentDatabase = documentDatabase;
    this.aliveUserDatabase = aliveUserDatabase;
    this.userDatabase = userDatabase;
    return true;
  }

  @Override
  public boolean helpRestartServer(int deadServerPort) {
    try {
      Registry registry = LocateRegistry.getRegistry(deadServerPort);
      ServerInterface stub = (ServerInterface) registry.lookup("Server" + deadServerPort);
      stub.restart(this.documentDatabase, this.aliveUserDatabase, this.userDatabase);
      return true;
    } catch (Exception e) {
      serverLog.log("Failed Restart Server! Exception: " + e.getMessage());
    }
    return false;
  }

  private UserDatabase initUserDB() {
    UserDatabase loadedUsersDB = loadUserDB();
    return loadedUsersDB == null ? new UserDatabase() : loadedUsersDB;
  }

  private UserDatabase loadUserDB() {
    try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(DATA_DIR + "UserDB.dat"))) {
      return (UserDatabase) input.readObject();
    } catch (IOException | ClassNotFoundException e) {
      return null;
    }
  }

  private DocumentDatabase initDocumentDB() {
    DocumentDatabase loadedDocumentsDB = loadDocumentDB();
    return loadedDocumentsDB == null ? new DocumentDatabase() : loadedDocumentsDB;
  }


  private DocumentDatabase loadDocumentDB() {
    try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(DATA_DIR + "DocDB.dat"))) {
      return (DocumentDatabase) input.readObject();
    } catch (IOException | ClassNotFoundException e) {
      return null;
    }
  }

  private Result twoPhaseCommit(UUID transactionID, CommitParams commitParams) {
    PrepareAck prepareAck = participantsPrepare(transactionID);
    if (prepareAck.numOfAcks < 5) {
      return new Result(0, "Request aborted.");
    } else {
      participantsAccept(transactionID, commitParams);
      return new Result(1, "Succeed");
    }
  }
}
