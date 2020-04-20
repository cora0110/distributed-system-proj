package client;

import chat.Receiver;
import chat.Sender;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;
import model.Message;
import model.Request;
import model.Result;
import model.User;
import server.CentralServer;
import server.CentralServerInterface;
import server.Server;
import server.ServerInterface;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client {

  public static int UDP_PORT = 1338;
  private static String CENTRAL_SERVER_HOST = "127.0.0.1";
  private static int CENTRAL_SERVER_RMI_PORT = 1200;
  private static String DATA_DIR;
  private String clientName;
  private ServerInterface serverInterface;
  private Sender messageSender;
  private Receiver messageReceiver;
  private NotiClientRunnable notiClientRunnable;
  private LocalSession session;
  private User user;

  public Client(String clientName) {
    try {
      this.clientName = clientName;
      DATA_DIR = "./client_data_" + clientName + "/";
      messageReceiver = new Receiver();
      checkDataDirectory();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Check if the data directory exists and is a valid directory, otherwise it creates it.
   */
  private static void checkDataDirectory() {
    File dataDir = new File(DATA_DIR);
    if (!dataDir.isDirectory() || !dataDir.exists()) dataDir.mkdirs();
  }


//  /**
//   * Checks if the JSON configuration file exists and the path is a valid one.
//   *
//   * @param filePath file path
//   * @return true if the the file exists and is a valid file, false otherwise
//   */
//  private boolean checkConfigFile(String filePath) {
//    File configFile = new File(filePath);
//    return configFile.isFile() && configFile.exists();
//  }

  /**
   * Program Entry Point.
   *
   * @param args command arguments
   */
  public static void main(String[] args) {
    if(args.length != 1) {
      System.out.println("Please specify a client name!");
      return;
    }

    String clientName = args[0];
    Client client = new Client(clientName);
    try {
      client.connect();
      client.commandDispatchingLoop();
    } catch (Exception ex) {
      client.printException(ex);
    } finally {
      try {
        client.notiClientRunnable.stop();
        client.messageReceiver.leave();
      } catch (Exception ignore) {
      }
    }
  }

  /**
   * Tries to connect to the {@code Server} running instance, create references to the streams for
   * {@code Socket} I/O operations and starts the {@code NotificationClientThread}.
   * <p>
   * This method needs to be called after {@code setup} method execution.
   *
   * @throws IOException if I/O errors occur
   * @see DataInputStream
   * @see DataOutputStream
   */
  private void connect() throws Exception {
    Registry centralRegistry = LocateRegistry.getRegistry(CENTRAL_SERVER_HOST, CENTRAL_SERVER_RMI_PORT);
    CentralServerInterface centralServer = (CentralServerInterface) centralRegistry.lookup(CentralServer.class.getSimpleName() + CENTRAL_SERVER_RMI_PORT);
    int port = centralServer.assignAliveServerToClient();
    Registry registry = LocateRegistry.getRegistry(port);
    serverInterface = (ServerInterface) registry.lookup(Server.class.getSimpleName() + port);
    System.out.println("Assigned to " + port);
    notiClientRunnable = new NotiClientRunnable(serverInterface);
    messageSender = new Sender();
    Thread thread = new Thread(messageReceiver);
    thread.start();
    if (messageSender == null) throw new IOException();
  }

  /**
   * Prints out and help message that sums up the {@code Client} commands list.
   */
  private void printCommandsHelp() {
    String message =
            "The following commands are available:\n" +
                    "  help: to show this help message\n\n" +
                    "  register USER PWD: to register a new account with username USER and password PWD\n" +
                    "  login USER PWD: to login using USER and PWD credentials\n" +
                    "  create DOC SEC: to create a new document named DOC and contains SEC sections\n" +
                    "  edit DOC SEC:(TMP) to edit the section SEC of DOC document (using TMP temporary filename)\n" +
                    "  endedit: to stop the current editing session\n" +
                    "  showsec DOC SEC:(OUT) to download the content of the SEC section of DOC document (using OUT output filename)\n" +
                    "  showdoc DOC:(OUT) to download the content concatenation of all the document's sections (using OUT output filename)\n" +
                    "  logout: to logout\n" +
                    "  list: to list all the documents you are able to see and edit\n" +
                    "  share USER DOC: to share a document with another user\n" +
                    "  news: to get all the news\n" +
                    "  receive: to retrieve all the unread chat messages\n" +
                    "  send TEXT: to send the TEXT message regarding the document being edited";
    System.out.println(message);
  }

  /**
   * Manages the commands dispatching loop that iterates over the {@code String} given to the
   * prompt, interprets the corresponding command and executes the respective action.
   *
   * @throws NotBoundException if a RMI registration error occurs
   * @throws IOException       if a registration I/O error occurs
   */
  private void commandDispatchingLoop() throws NotBoundException, IOException {
    String command = null;
    Scanner input = new Scanner(System.in);
    boolean isAlive = true;
    while (isAlive) {
      System.out.print("collaborative@127.0.0.1# ");
      String argsLine = input.nextLine();
      String[] args = argsLine.split(" ");
      if (argsLine.length() > 0 && args.length > 0) {
        command = args[0];
        try {
          switch (command) {
            case "exit":
            case "quit":
              if (session == null) {
                isAlive = false;
              } else {
                System.err.println("Please logout first.");
              }
              break;
            case "register":
              if (args.length > 2) {
                String username = args[1];
                String password = args[2];
                register(username, password);
              } else throw new IllegalArgumentException();
              break;
            case "login":
              if (args.length > 2) {
                String username = args[1];
                String password = args[2];
                // TODO: 4/17/20
                login(username, password);
              } else throw new IllegalArgumentException();
              break;
            case "create":
              if (args.length > 2) {
                try {
                  String docName = args[1];
                  int secNum = Integer.valueOf(args[2]);
                  create(docName, secNum);
                } catch (NumberFormatException ex) {
                  throw new IllegalArgumentException();
                }
              } else throw new IllegalArgumentException();
              break;
            case "edit":
              if (args.length > 2) {
                String tmpFile = null;
                if (args.length > 3) tmpFile = args[3];
                try {
                  String docName = args[1];
                  int secNum = Integer.valueOf(args[2]);
                  edit(docName, secNum, tmpFile);
                } catch (NumberFormatException ex) {
                  throw new IllegalArgumentException();
                }
              } else throw new IllegalArgumentException();
              break;
            case "endedit":
              editEnd();
              break;
            case "showsec":
              if (args.length > 2) {
                String outputFile = null;
                if (args.length > 3) outputFile = args[3];
                try {
                  String docName = args[1];
                  int secNum = Integer.valueOf(args[2]);
                  showSection(docName, secNum, outputFile);
                } catch (NumberFormatException ex) {
                  throw new IllegalArgumentException();
                }
              } else throw new IllegalArgumentException();
              break;
            case "showdoc":
              if (args.length > 1) {
                String docName = args[1];
                String outputFile = null;
                if (args.length > 2) outputFile = args[2];
                showDocument(docName, outputFile);
              } else throw new IllegalArgumentException();
              break;
            case "logout":
              logout();
              break;
            case "list":
              documentsList();
              break;
            case "share":
              if (args.length > 2) {
                String username = args[1];
                String docName = args[2];
                share(username, docName);
              } else throw new IllegalArgumentException();
              break;
            case "news":
              printNews();
              break;
            case "receive":
              showMessages();
              break;
            case "send":
              if (args.length > 1) {
                String text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                sendMessage(text);
              } else throw new IllegalArgumentException();
              break;
            case "help":
              printCommandsHelp();
              break;
            default:
              throw new IllegalArgumentException();
          }
        } catch (IllegalArgumentException ex) {
          System.err.println("Unsupported arguments. Please try again.");
        } catch (Exception e) {
          System.err.println("Internal error. Please try again.");
        }
      }
    }
  }

  /**
   * Retrieve an available server from central server.
   *
   * @return assigned server
   */
  private ServerInterface retrieveServer() {
    try {
      Registry centralRegistry = LocateRegistry.getRegistry(CENTRAL_SERVER_HOST, CENTRAL_SERVER_RMI_PORT);
      CentralServerInterface central = (CentralServerInterface) centralRegistry.lookup(CentralServerInterface.class.getSimpleName());
      int serverPort = central.assignAliveServerToClient();

      Registry registry = LocateRegistry.getRegistry(serverPort);
      ServerInterface serverInterface = (ServerInterface) registry.lookup(ServerInterface.class.getSimpleName());
      return serverInterface;
    } catch (Exception e) {
      throw new RuntimeException("Unable to retrieve server.");
    }
  }


  /**
   * Register a new user.
   */
  private void register(String username, String password) throws Exception {
    Result result = serverInterface.createUser(new User(username, password));
    if(result.getStatus() == 1) {
      System.out.println("User " + username + " registered successfully!");
    } else {
      System.err.println(result.getMessage());
    }
  }

  /**
   * Authenticates the user into system trying to validate the user's password and informs the
   * server about its notification's port used by its {@code NotificationClientThread}.
   * <p>
   * It starts a new {@code LocalSession} object that collects all the session's information.
   *
   * @param username user username
   * @param password user password
   */
  private void login(String username, String password) throws Exception {
    if (session == null) {
      Result result = serverInterface.login(new User(username, password));
      if (result.getStatus() == 0) {
        System.err.println(result.getMessage());
        return;
      }
      String token = result.getMessage();
      user = new User(username);
      notiClientRunnable.setUser(user);
      new Thread(notiClientRunnable).start();
      session = new LocalSession(token, user);
      System.out.println("Logged in successfully as " + username);
    } else {
      System.err.println("You're already logged in.");
    }
  }

  /**
   * Kills session, stops the {@code NotificationClientThread} and clears the notifications' list.
   */
  private void logout() throws Exception {
    if (session != null) {
      if (!session.isEditing()) {
        serverInterface.logout(new User(session.getUser().getUsername()));
        session = null;
        notiClientRunnable.clearNotificationList();
        notiClientRunnable.setUser(null);
        notiClientRunnable.stop();
        System.out.println("Successfully logged out.");
      } else System.err.println("You should 'stopedit' before logging out");
    } else System.err.println("You're not logged in");
  }

  /**
   * Create a new Document.
   *
   * @param docName   new document name
   * @param secNumber number of sections of the new document
   */
  private void create(String docName, int secNumber) throws Exception {
    if (session != null) {
      User user = new User(session.getUser().getUsername());
      Request request = new Request();
      request.setToken(session.getSessionToken());
      request.setDocName(docName);
      request.setSectionNum(secNumber);
      serverInterface.createDocument(user, request);
    } else System.err.println("You're not logged in");
  }

  /**
   * Starts an edit session for a specific {@code Document}'s {@code Section}.
   * <p>
   * Initializes a new multicast group for a {@code MessageReceiver} object too.
   *
   * @param docName        document filename
   * @param secNumber      section index
   * @param chosenFilename output filename
   */
  private void edit(String docName, int secNumber, String chosenFilename) {
    if (session != null) {
      String filepath = chosenFilename != null ? chosenFilename : DATA_DIR + docName + "_" + secNumber;
      try (FileChannel fileChannel = FileChannel.open(Paths.get(filepath), StandardOpenOption.CREATE,
              StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
           OutputStream fileStream = Channels.newOutputStream(fileChannel)) {
        Request request = new Request();
        request.setDocName(docName);
        request.setSectionNum(secNumber);
        Result result = serverInterface.edit(new User(session.getUser().getUsername()), request);
        InputStream inputStream = RemoteInputStreamClient.wrap(result.getRemoteInputStream());
        fileStream.write(inputStream.read());

        session.setOccupiedFilename(filepath);
        long address = Long.parseLong(result.getMessage());
        try {
          messageReceiver.setNewGroup(address);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      } catch (IOException ex) {
        printException(ex);
      }
    } else System.err.println("You're not logged in");
  }

  /**
   * Stops a {@code Document}'s {@code Section} editing and imposes the {@code MessageReceiver} to
   * leave the actual multicast group.
   */
  private void editEnd() {
    if (session != null) {
      if (session.isEditing()) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(session.getOccupiedFilename()), StandardOpenOption.READ);
             InputStream stream = Channels.newInputStream(fileChannel)) {
          session.setOccupiedFilename(null);
          RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(stream);
          Request request = new Request();
          String[] path = session.getOccupiedFilename().split("_");
          request.setDocName(path[0]);
          request.setSectionNum(Integer.parseInt(path[1]));
          request.setRemoteInputStream(remoteFileData);
          serverInterface.editEnd(new User(session.getUser().getUsername()), request);
          messageReceiver.setNewGroup(0L);
        } catch (Exception ex) {
          printException(ex);
        }
      } else System.err.println("You're not editing any section");
    } else System.err.println("You're not logged in");
  }

  /**
   * Reads the content of the requested {@code Section} and, if somebody is editing it, returns the
   * editor's name.
   * <p>
   * If the {@code chosenFilename} is null, the default name is used: {@code docName}_{@code
   * secNumber}.
   *
   * @param docName        document's name
   * @param secNumber      target section
   * @param chosenFilename output filename or null
   */
  private void showSection(String docName, int secNumber, String chosenFilename) {
    if (session != null) {
      String filename = chosenFilename != null ? chosenFilename : DATA_DIR + docName + "_" + secNumber;
      try (FileChannel fileChannel = FileChannel.open(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
           OutputStream fileStream = Channels.newOutputStream(fileChannel)) {
        Request request = new Request();
        request.setDocName(docName);
        request.setSectionNum(secNumber);
        request.setToken(session.getSessionToken());
        Result result = serverInterface.showSection(new User(session.getUser().getUsername()), request);
        InputStream inputStream = RemoteInputStreamClient.wrap(result.getRemoteInputStream());
        String editor = result.getMessage();
        fileStream.write(inputStream.read());
        if (!editor.equals("None")) {
          System.out.println(String.format("%s is editing the section right now", editor));
        } else System.out.println("None is editing this section");
      } catch (IOException ex) {
        printException(ex);
      }
    } else System.err.println("You're not logged in");
  }

  /**
   * Gets the list of {@code Document}s on which the {@code User} has permissions.
   */
  private void documentsList() throws Exception {
    if (session != null) {
      Request request = new Request();
      request.setToken(session.getSessionToken());
      Result result = serverInterface.listOwnedDocs(new User(session.getUser().getUsername()), request);
      System.out.println(result.getMessage());
    } else System.err.println("You're not logged in");
  }

  /**
   * Shares a document with another {@code User}, giving him the permission to modify and see it.
   * <p>
   * When a {@code User} receives new permissions, a notification will be delivered to him.
   *
   * @param user    user's username
   * @param docName document's name
   */
  private void share(String user, String docName) throws Exception {
    Request request = new Request();
    request.setToken(session.getSessionToken());
    serverInterface.shareDoc(new User(session.getUser().getUsername()), request);
  }

  /**
   * Gets the entire requested {@code Document} concatenating all its {@code Section}s together.
   * <p>
   * If the {@code outputName} is null, the {@code docName} value is used.
   *
   * @param docName    document's name
   * @param outputName output filename
   */
  private void showDocument(String docName, String outputName) {
    if (session != null) {
      String filename = DATA_DIR + (outputName == null ? docName : outputName);
      try (FileChannel fileChannel = FileChannel.open(Paths.get(filename), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
           OutputStream fileStream = Channels.newOutputStream(fileChannel)) {
        Request request = new Request();
        request.setDocName(docName);
        request.setToken(session.getSessionToken());
        Result result = serverInterface.showDocumentContent(new User(session.getUser().getUsername()), request);
        InputStream inputStream = RemoteInputStreamClient.wrap(result.getRemoteInputStream());
        String message = result.getMessage();
        fileStream.write(inputStream.read());
        if (!message.equals("None")) {
          System.out.println(String.format("These are the on editing sections: %s", message));
        } else System.out.println("None is editing this document");
      } catch (IOException ex) {
        printException(ex);
      }
    } else System.err.println("You're not logged in");
  }


  /**
   * Prints out all the notifications collected since the last method invocation.
   */
  private void printNews() {
    if (session != null) {
      List<String> notifications = notiClientRunnable.getAllNotifications();
      if (!notifications.isEmpty())
        System.out.println("You have permission on these new documents: " + String.join(",", notifications));
      else System.err.println("No news available");
    } else System.err.println("You're not logged in");
  }

  /**
   * Shows all the received {@code ChatMessage}s received since the last method invocation.
   */
  private void showMessages() {
    if (session != null) {
      if (session.isEditing()) {
        List<Message> messages = messageReceiver.retrieve();
        for (Message message : messages)
          System.out.println(message);
      } else System.err.println("You're not editing any document");
    } else System.err.println("You're not logged in");
  }

  /**
   * Sends a new {@code ChatMessage} UDP multicast packet to every listening {@code
   * MessageReceiver}.
   *
   * @param text message text
   */
  private void sendMessage(String text) {
    if (session != null) {
      if (session.isEditing()) {
        InetAddress multicastAddress;
        if ((multicastAddress = messageReceiver.getAddress()) != null) {
          try {
            Message message = new Message(session.getUser().getUsername(), text, System.currentTimeMillis());
            InetSocketAddress groupAddress = new InetSocketAddress(multicastAddress, UDP_PORT);
            messageSender.sendMessage(message, groupAddress);
          } catch (Exception ex) {
            printException(ex);
          }
        } else System.err.println("Generic message sending error");
      } else System.err.println("You're not editing any document");
    } else System.err.println("You're not logged in");
  }

  /**
   * Prints out a generic {@code Exception} in a friendly format.
   *
   * @param ex
   */
  private void printException(Exception ex) {
    System.err.println(ex.getMessage());
  }


}
