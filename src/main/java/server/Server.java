package server;

import chat.ChatManager;
import com.healthmarketscience.rmiio.RemoteInputStream;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import model.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server extends UnicastRemoteObject implements ServerInterface {
  public int currPort;
  private final String DATA_DIR;
  private final String USER_DB_NAME = "UserDB.dat";
  private final String DOC_DB_NAME = "DocDB.dat";
  public String serverName;
  private int centralPort;
  private ServerLogger serverLogger;
  private DocumentDatabase documentDatabase;
  private AliveUserDatabase aliveUserDatabase;
  private UserDatabase userDatabase;
  private ChatManager chatManager;
  // temporarily store CommitParams info before a transaction commits
  private ConcurrentMap<UUID, CommitParams> tempStorage;
  private ConcurrentMap<UUID, ConcurrentMap<Integer, Boolean>> prepareResponseMap;
  private ConcurrentMap<UUID, ConcurrentMap<Integer, Boolean>> commitResponseMap;

  public Server(int currPort, int centralPort) throws RemoteException {
    this.currPort = currPort;
    this.serverName = Server.class.getSimpleName() + currPort;
    this.centralPort = centralPort;
    DATA_DIR = "./server_data_" + currPort + "/";
    createDataDirectory();
    serverLogger = new ServerLogger();
    userDatabase = initUserDB();
    documentDatabase = initDocumentDB();
    aliveUserDatabase = new AliveUserDatabase();
    chatManager = new ChatManager();

    tempStorage = new ConcurrentHashMap<>();
    prepareResponseMap = new ConcurrentHashMap<>();
    commitResponseMap = new ConcurrentHashMap<>();

    bindRMI();

    // store memory database when shutting down with shutdown hook
    userDatabase = initUserDB();
    documentDatabase = initDocumentDB();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.out.println("Server is shutting down...");
      storeUsersDB();
      storeDocumentsDB();
    }));
  }

  /**
   * Stores UserDB object through serialization.
   */
  private boolean storeUsersDB() {
    try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(DATA_DIR + USER_DB_NAME))) {
      output.writeObject(userDatabase);
      return true;
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * Stores DocumentsDatabase object through serialization.
   */
  private boolean storeDocumentsDB() {
    try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(DATA_DIR + DOC_DB_NAME))) {
      output.writeObject(documentDatabase);
      return true;
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * Binds RMI
   */
  public void bindRMI() {
    try {
      Registry registry = LocateRegistry.createRegistry(currPort);
      registry.rebind(serverName, this);
      serverLogger.log(serverName + " is running...");
    } catch (Exception e) {
      serverLogger.log(e.getMessage());
    }
  }

  @Override
  public Result createUser(User user) throws RemoteException {
    String username = user.getUsername();
    if (userDatabase.isUsernameAvailable(username)) {
      // TODO: 4/17/20
      CommitParams commitParams = new CommitParams();
      commitParams.setUser(user);
      commitParams.setCommitEnum(CommitEnum.CREATE_USER);
      commitParams.setSectionNum(-1);
      // TODO: 4/19/20
//      UserDatabase cloneUserDB = SerializationUtils.clone(userDatabase);
      commitParams.setUserDatabase(userDatabase);

      Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);
      if (result.getStatus() == 1) {
        serverLogger.log(serverName, CommitEnum.CREATE_USER + ": SUCCESS");
        return new Result(1, "Create user succeed");
      }
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
        // TODO: 4/17/20
        CommitParams commitParams = new CommitParams();
        commitParams.setUser(user);
        commitParams.setCommitEnum(CommitEnum.LOGIN);
        commitParams.setSectionNum(-1);
        commitParams.setAliveUserDatabase(aliveUserDatabase);

        // TODO: 2PC AND RETURN TOKEN
        Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);
        if (result.getStatus() == 0) {
          return new Result(0, "Request aborted.");
        }
        String token = aliveUserDatabase.getTokenByUser(user.getUsername());

        if (token != null) {
          serverLogger.log(serverName, CommitEnum.LOGIN + ": SUCCESS");
          serverLogger.log("New user logged in: " + user.getUsername());
          return new Result(1, token);
        } else {
          return new Result(0, "Token generation failure while logging in.");
        }
      } else {
        return new Result(0, "Unregistered or password do not match.");
      }
    }
  }

  @Override
  public Result logout(User user) throws RemoteException {
    // TODO: ANY NEED TO DO 2PC?
    CommitParams commitParams = new CommitParams();
    commitParams.setUser(user);
    commitParams.setCommitEnum(CommitEnum.LOGOUT);
    commitParams.setSectionNum(-1);
    // TODO: 4/19/20 should be an updated temporary param
    commitParams.setAliveUserDatabase(aliveUserDatabase);
    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);

    if (result.getStatus() == 1) {
      System.out.println("User logged out: " + user.getUsername());
      return new Result(1, "succeed");
    } else {
      return new Result(0, "Request aborted.");
    }

  }

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

    Section section = document.getSectionByIndex(request.getSectionNum());
    if (section == null) {
      return new Result(0, "Section does not exist.");
    }

    User editingUser = section.getOccupant();
    if (editingUser != null) {
      return new Result(0, "The section is being edited");
    }

    CommitParams commitParams = new CommitParams();
    commitParams.setUser(user);
    commitParams.setCommitEnum(CommitEnum.EDIT);
    commitParams.setDocName(request.getDocName());
    commitParams.setSectionNum(request.getSectionNum());
    // TODO: 4/19/20 should be an updated temporary param
    commitParams.setDocumentDatabase(documentDatabase);
    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);

    if (result.getStatus() == 0) {
      return new Result(0, "Request aborted.");
    }

    try {
      InputStream inputStream = new FileInputStream(section.getPath());
      SimpleRemoteInputStream remoteInputStream = new SimpleRemoteInputStream(inputStream);
      // assign multicast address
      long chatAddress = chatManager.getChatAddress(document);
      return new Result(1, String.valueOf(chatAddress), remoteInputStream);
    } catch (IOException ioe) {
      return new Result(0, "IO Exception while accessing the section");
    }
  }

  @Override
  public Result editEnd(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.equals(aliveUserDatabase.getUserByToken(request.getToken()))) {
      return new Result(0, "User does not match token.");
    }

    Document document = documentDatabase.getDocumentByName(request.getDocName());
    if (document == null) {
      return new Result(0, "Document does not exists.");
    }

    if (!document.hasPermit(user)) {
      return new Result(0, "You do not have access.");
    }

    Section section = document.getSectionByIndex(request.getSectionNum());
    if (section == null) {
      return new Result(0, "Section does not exist.");
    }

    User editingUser = section.getOccupant();
    if (!editingUser.equals(user)) {
      return new Result(0, "The section is being edited by other");
    }

    CommitParams commitParams = new CommitParams();
    commitParams.setUser(user);
    commitParams.setCommitEnum(CommitEnum.EDIT_END);
    commitParams.setDocName(request.getDocName());
    commitParams.setSectionNum(request.getSectionNum());
    commitParams.setInputStream(request.getRemoteInputStream());
    // TODO: 4/19/20 should be an updated temporary param, process stream.
    commitParams.setDocumentDatabase(documentDatabase);

    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);

    if (result.getStatus() == 0) {
      return new Result(0, "Request aborted");
    } else {
      return new Result(1, "Succeed");
    }
  }

  @Override
  public Result createDocument(User user, Request request) throws RemoteException {
    if (!aliveUserDatabase.isLoggedIn(user.getUsername())) {
      return new Result(0, "Not logged in.");
    }

    if (!user.getUsername().equals(aliveUserDatabase.getUserByToken(request.getToken()).getUsername())) {
      return new Result(0, "User does not match token.");
    }

    Document document = documentDatabase.getDocumentByName(request.getDocName());
    if (document != null) {
      return new Result(0, "Document already exists.");
    }

    CommitParams commitParams = new CommitParams();
    commitParams.setUser(user);
    commitParams.setCommitEnum(CommitEnum.CREATE_DOCUMENT);
    commitParams.setDocName(request.getDocName());
    commitParams.setSectionNum(request.getSectionNum());
    // TODO: 4/19/20 should be an updated temporary param
    commitParams.setDocumentDatabase(documentDatabase);

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

    Section section = document.getSectionByIndex(request.getSectionNum());
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
      for (Section s : document.getSections()) {
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

    if (!document.getCreator().equals(user)) {
      return new Result(0, "You do not have access.");
    }

    CommitParams commitParams = new CommitParams();
    commitParams.setUser(user);
    commitParams.setCommitEnum(CommitEnum.SHARE);
    commitParams.setDocName(request.getDocName());
    commitParams.setSectionNum(request.getSectionNum());
    // TODO: 4/19/20 should be an updated temporary param
    commitParams.setDocumentDatabase(documentDatabase);

    Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);

    //TODO push notification test
    document.addAuthor(request.getTargetUser());
    User sharedUser = userDatabase.getUserByUsername(request.getTargetUser().getUsername());
    sharedUser.pushNewNotification("User " +
            request.getTargetUser().getUsername() + " can now access the document " + document.getName());

    if (result.getStatus() == 1) {
      return new Result(1, "Succeed");
    } else {
      return new Result(0, "Request aborted");
    }
  }

  @Override
  public Result getNotifications(User user) throws RemoteException {
    User userDB = userDatabase.getUserByUsername(user.getUsername());
    List<String> unreadNotifications = null;
    if (userDB != null) {
      unreadNotifications = userDB.getUnreadNotifications();
    }
    Result result = new Result();
    result.setUnreadNotifications(unreadNotifications);
    return result;
  }

  @Override
  public void kill() throws RemoteException {
    Registry registry = LocateRegistry.getRegistry(this.currPort);
    try {
      // TODO: 4/17/20 add store database. shutdown hook
      registry.unbind(this.serverName);
    } catch (NotBoundException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean recoverData(BackupData backupData) {
    this.documentDatabase = backupData.getDocumentDatabase();
    this.userDatabase = backupData.getUserDatabase();
    this.aliveUserDatabase = backupData.getAliveUserDatabase();
    this.chatManager = backupData.getChatManager();

    // clear previous data
    try {
      FileUtils.deleteDirectory(new File(DATA_DIR));
      createDataDirectory();
    } catch (IOException e) {
      serverLogger.log(e.getMessage());
    }
    Map<String, RemoteInputStream> fileStreamMap = backupData.getFileStreamMap();
    for (String path : fileStreamMap.keySet()) {
      try {
        InputStream inputStream = getInputStream(fileStreamMap.get(path));
        if (inputStream == null) return false;
        FileUtils.copyInputStreamToFile(inputStream, new File(path));
      } catch (IOException e) {
        serverLogger.log(serverName, e.getMessage());
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean helpRecoverData(int targetPort) {
    DocumentDatabase documentDatabase = this.documentDatabase;
    UserDatabase userDatabase = this.userDatabase;
    AliveUserDatabase aliveUserDatabase = this.aliveUserDatabase;
    ChatManager chatManager = this.chatManager;
    Map<String, RemoteInputStream> fileStreamMap = new HashMap<>();

    // user file
    // DATA_DIR + "DocDB.dat"
    String targetDataDir = "./server_data" + targetPort + "/";
    // put userDatabase dat file
    fileStreamMap.put(targetDataDir + "UserDB.dat", getRemoteInputStream(DATA_DIR + "UserDB.dat"));
    // put DocumentDatabase dat file
    fileStreamMap.put(targetDataDir + "DocDB.dat", getRemoteInputStream(DATA_DIR + "DocDB.dat"));

    // put section files
    for (Document doc : documentDatabase.getDocuments()) {
      for (Section section : doc.getSections()) {
        String currPath = section.getPath();
        String pattern = "(.*data)([0-9]+)(/.*)";
        // replace port in the path
        String targetPath = currPath.replaceAll(pattern, "$1" + targetPort + "$3");
        fileStreamMap.put(targetPath, getRemoteInputStream(currPath));
      }
    }
    BackupData backupData = new BackupData(documentDatabase, userDatabase, aliveUserDatabase, chatManager, fileStreamMap);

    try {
      Registry registry = LocateRegistry.getRegistry(targetPort);
      ServerInterface stub = (ServerInterface) registry.lookup(Server.class.getSimpleName() + targetPort);
      stub.recoverData(backupData);
      return true;
    } catch (Exception e) {
      serverLogger.log("Failed Restart Server! Exception: " + e.getMessage());
    }
    return false;
  }

  private RemoteInputStream getRemoteInputStream(String filePath) {
    try (FileChannel fileChannel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ);
         InputStream stream = Channels.newInputStream(fileChannel)) {
      return new SimpleRemoteInputStream(stream);
    } catch (Exception e) {
      serverLogger.log("Exception: " + e.getMessage());
    }
    return null;
  }

  private InputStream getInputStream(RemoteInputStream remoteInputStream) {
    try {
      RemoteInputStreamClient.wrap(remoteInputStream);
    } catch (IOException e) {
      serverLogger.log(e.getMessage());
    }
    return null;
  }

  private UserDatabase initUserDB() {
    UserDatabase loadedUsersDB = loadUserDB();
    return loadedUsersDB == null ? new UserDatabase() : loadedUsersDB;
  }

  private UserDatabase loadUserDB() {
    try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(DATA_DIR + USER_DB_NAME))) {
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
    try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(DATA_DIR + DOC_DB_NAME))) {
      return (DocumentDatabase) input.readObject();
    } catch (IOException | ClassNotFoundException e) {
      return null;
    }
  }

  private void createDataDirectory() {
    File dataDir = new File(DATA_DIR);
    if (!dataDir.isDirectory() || !dataDir.exists()) dataDir.mkdirs();
  }

  private Result twoPhaseCommit(UUID transactionID, CommitParams commitParams) {
    // update database stored in commitParams
    commitParams = updateCommitParamsDatabase(commitParams);
    if (!prepare(transactionID, commitParams)) {
      commitOrAbort(transactionID, false);
      return new Result(0, "Request Aborted.");
    } else {
      commitOrAbort(transactionID, true);
      return new Result(1, "Request Committed.");
    }
  }

  CommitParams updateCommitParamsDatabase(CommitParams commitParams) {
    switch (commitParams.getCommitEnum()) {
      // create user: add new user to userDatabase
      case CREATE_USER:
        commitParams.getUserDatabase().addNewUser(commitParams.getUser().getUsername(), commitParams.getUser().getPassword());
        break;
      // login: create new OnlineUserRecord (generate token) and put into aliveUserDatabase
      case LOGIN:
        commitParams.getAliveUserDatabase().login(commitParams.getUser());
        break;
      // logout: set token to null
      case LOGOUT:
        String username = commitParams.getUser().getUsername();
        commitParams.getAliveUserDatabase().getOnlineUserRecord(username).setToken(null);
        break;
      // edit: set occupant of the section in documentDatabase
      case EDIT:
        String docName = commitParams.getDocName();
        int sectionNum = commitParams.getSectionNum();
        commitParams.getDocumentDatabase().getDocumentByName(docName).
                getSectionByIndex(sectionNum).occupy(commitParams.getUser());
        break;
      // edit end: write input stream into section path
      // no need to update database
      case EDIT_END:
        break;
      // create document: create a new document in documentDatabase
      case CREATE_DOCUMENT:
        commitParams.getDocumentDatabase().createNewDocument(DATA_DIR,
                commitParams.getSectionNum(), commitParams.getDocName(), commitParams.getUser());
        break;
      // share doc: add user to authors of a document in documentDatabase
      case SHARE:
        commitParams.getDocumentDatabase().
                getDocumentByName(commitParams.getDocName()).addAuthor(commitParams.getUser());
        break;
    }
    return commitParams;
  }

  @Override
  public boolean prepare(UUID transactionID, CommitParams commitParams) {
    if (getServerStatus(currPort) != 0) return false;
    // change current server status: Empty -> Busy
    setServerStatus(currPort, 1);
    // add the <transactionID, CommitParams> to tempStorage
    addToTempStorage(transactionID, commitParams);

    int[] peers = getPeers(currPort);
    int numOfPeers = peers.length;

    // put transactionID into prepareResponseMap
    prepareResponseMap.put(transactionID, new ConcurrentHashMap<>());
    serverLogger.log(serverName, "Prepare: sent");
    for (int peerPort : peers) {
      try {
        Registry registry = LocateRegistry.getRegistry(peerPort);
        ServerInterface stub = (ServerInterface) registry.lookup(Server.class.getSimpleName() + peerPort);
        boolean prepareAck = stub.receivePrepare(transactionID, commitParams);
        prepareResponseMap.get(transactionID).put(peerPort, prepareAck);
      } catch (Exception e) {
        serverLogger.log(serverName, "Exception: " + e.getMessage());
      }
    }

    int ackCount = 0;
    int agreeAckCount = 0;
    // retry for three times
    int retry = 3;
    while (retry > 0) {
      if (retry < 3) {
        try {
          // pause for 1 secs
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          serverLogger.log(serverName, e.getMessage());
        }
      }
      ackCount = 0;
      agreeAckCount = 0;
      retry--;
      Map<Integer, Boolean> ackMap = prepareResponseMap.get(transactionID);
      for (int peerPort : peers) {
        if (ackMap.get(peerPort) != null) {
          if (ackMap.get(peerPort)) {
            ackCount++;
            agreeAckCount++;
          } else {
            ackCount++;
          }
        }
      }

      if (ackCount == numOfPeers) {
        return agreeAckCount == numOfPeers;
      }
    }

    // change status of dead server
    for (int peerPort : peers) {
      if (prepareResponseMap.get(transactionID).get(peerPort) == null) {
        setServerStatus(peerPort, 2);
        sendMessageToCentral(Server.class.getSimpleName() + peerPort + " is down!");
      }
    }

    // if 0 abort ack && receive agree acks from more than half peers, commit, otherwise abort
    if (ackCount == agreeAckCount && ackCount >= (numOfPeers / 2 + 1)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean receivePrepare(UUID transactionID, CommitParams commitParams) {
    serverLogger.log(serverName, "Prepare: received");
    if (getServerStatus(currPort) != 0) {
      serverLogger.log(serverName, "Abort: sent");
      return false;
    } else {
      setServerStatus(currPort, 1);
      addToTempStorage(transactionID, commitParams);
      serverLogger.log(serverName, "Agree: sent");
      return true;
    }

  }

  @Override
  public void commitOrAbort(UUID transactionID, boolean ack) {
    int[] peers = getPeers(currPort);
    int numOfPeers = peers.length;
    commitResponseMap.put(transactionID, new ConcurrentHashMap<>());

    if (ack) {
      serverLogger.log(serverName, "Commit: sent");
      for (int peerPort : peers) {
        try {
          Registry registry = LocateRegistry.getRegistry(peerPort);
          ServerInterface stub = (ServerInterface) registry.lookup(Server.class.getSimpleName() + peerPort);
          boolean commitAck = stub.receiveCommit(transactionID);
          commitResponseMap.get(transactionID).put(peerPort, commitAck);
        } catch (Exception e) {
          serverLogger.log(serverName, "Exception: " + e.getMessage());
        }
      }
    } else {
      serverLogger.log(serverName, "Abort: sent");
      for (int peerPort : peers) {
        Boolean prepareAck = prepareResponseMap.get(transactionID).get(peerPort);
        if (prepareAck != null && prepareAck) {
          try {
            Registry registry = LocateRegistry.getRegistry(peerPort);
            ServerInterface stub = (ServerInterface) registry.lookup(Server.class.getSimpleName() + currPort);
            boolean commitAck = stub.receiveAbort(transactionID);
            commitResponseMap.get(transactionID).put(peerPort, commitAck);
          } catch (Exception e) {
            serverLogger.log(serverName, "Exception: " + e.getMessage());
          }
        }
      }
    }

    int ackCount;
    int retry = 3;
    while (retry > 0) {
      if (retry < 3) {
        try {
          // pause for 2 secs
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          serverLogger.log(serverName, e.getMessage());
        }
      }

      ackCount = 0;
      retry--;

      Map<Integer, Boolean> ackMap = commitResponseMap.get(transactionID);

      for (int peerPort : peers) {
        if (ackMap.get(peerPort) != null) {
          ackCount++;
        }
      }

      if (ackCount == numOfPeers) break;
    }

    for (int peerPort : peers) {
      if (commitResponseMap.get(transactionID).get(peerPort) == null) {
        setServerStatus(peerPort, 2);
        sendMessageToCentral(Server.class.getSimpleName() + peerPort + " is down!");
      }
    }

    if (ack) {
      CommitParams commitParams = tempStorage.get(transactionID);
      if (commitParams == null) {
        throw new IllegalArgumentException("The commitParams need to commit cannot be found.");
      }
      executeCommit(commitParams);
    }
    // clean up all temp data and state
    tempStorage.remove(transactionID);
    prepareResponseMap.remove(transactionID);
    commitResponseMap.remove(transactionID);
    setServerStatus(currPort, 0);
  }

  @Override
  public boolean receiveCommit(UUID transactionID) {
    serverLogger.log(serverName, "Commit: received");
    CommitParams commitParams = tempStorage.get(transactionID);
    if (commitParams == null) {
      throw new IllegalArgumentException("The commitParams need to commit cannot be found.");
    }
    executeCommit(commitParams);
    tempStorage.remove(transactionID);
    setServerStatus(currPort, 0);
    return true;
  }

  @Override
  public boolean receiveAbort(UUID transactionID) {
    serverLogger.log(serverName, "Abort: received");
    tempStorage.remove(transactionID);
    setServerStatus(currPort, 0);
    return true;
  }

  @Override
  public void executeCommit(CommitParams commitParams) {
    switch (commitParams.getCommitEnum()) {
      case CREATE_USER:
        this.userDatabase = commitParams.getUserDatabase();
        break;
      case LOGIN:
      case LOGOUT:
        this.aliveUserDatabase = commitParams.getAliveUserDatabase();
        break;
      case EDIT:
      case CREATE_DOCUMENT:
      case SHARE:
        this.documentDatabase = commitParams.getDocumentDatabase();
        break;
      case EDIT_END:
        OutputStream fileStream = null;
        try {
          Section editingSection = documentDatabase.
                  getDocumentByName(commitParams.getDocName()).
                  getSectionByIndex(commitParams.getSectionNum());
          fileStream = editingSection.getWriteStream();
          InputStream inputStream = RemoteInputStreamClient.wrap(commitParams.getInputStream());
          fileStream.write(inputStream.read());
        } catch (IOException e) {
          serverLogger.log(serverName, e.getMessage());
        }
        if (fileStream != null) {
          try {
            fileStream.close();
          } catch (IOException ex) {
            serverLogger.log(serverName, ex.getMessage());
          }
        }
        break;
    }
  }

  private int getServerStatus(int port) {
    try {
      Registry registry = LocateRegistry.getRegistry(centralPort);
      CentralServerInterface stub = (CentralServerInterface) registry.lookup(CentralServer.class.getSimpleName() + centralPort);
      return stub.getServerStatus(port);
    } catch (Exception e) {
      serverLogger.log(serverName, "Exception: " + e.getMessage());
      return -1;
    }
  }

  private void setServerStatus(int port, int status) {
    try {
      Registry registry = LocateRegistry.getRegistry(centralPort);
      CentralServerInterface stub = (CentralServerInterface) registry.lookup(CentralServer.class.getSimpleName() + centralPort);
      stub.setServerStatus(port, status);
    } catch (Exception e) {
      serverLogger.log(serverName, "Exception: " + e.getMessage());
    }
  }

  private int[] getPeers(int currPort) {
    try {
      Registry registry = LocateRegistry.getRegistry(centralPort);
      CentralServerInterface stub = (CentralServerInterface) registry.lookup(CentralServer.class.getSimpleName() + centralPort);
      return stub.getPeers(currPort);
    } catch (Exception e) {
      serverLogger.log(serverName, "Exception: " + e.getMessage());
      return null;
    }
  }

  private void sendMessageToCentral(String message) {
    try {
      Registry registry = LocateRegistry.getRegistry(centralPort);
      CentralServerInterface stub = (CentralServerInterface) registry.lookup(CentralServer.class.getSimpleName() + centralPort);
      stub.receiveNotification(message);
    } catch (Exception e) {
      serverLogger.log(serverName, "Exception: " + e.getMessage());
    }
  }

  private void addToTempStorage(UUID transactionID, CommitParams commitParams) {
    tempStorage.put(transactionID, commitParams);
  }
}
