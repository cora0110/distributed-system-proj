package com.distributed.client;

import com.distributed.chat.Receiver;
import com.distributed.chat.Sender;
import com.distributed.model.Message;
import com.distributed.model.Request;
import com.distributed.model.Result;
import com.distributed.model.User;
import com.distributed.server.CentralServer;
import com.distributed.server.CentralServerInterface;
import com.distributed.server.Server;
import com.distributed.server.ServerInterface;
import com.healthmarketscience.rmiio.RemoteInputStreamClient;
import com.healthmarketscience.rmiio.RemoteInputStreamServer;
import com.healthmarketscience.rmiio.SimpleRemoteInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

  private static String CENTRAL_SERVER_HOST = "127.0.0.1";
  private static int CENTRAL_SERVER_RMI_PORT = 1200;
  public static int UDP_PORT = 4567;
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

  /**
   * Program Entry Point.
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Please specify a client name!");
      return;
    }

    System.setProperty("java.net.preferIPv4Stack", "true");
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
      } catch (Exception ignore) {
      }
    }
  }

  /**
   * Connect to a server, create references to the streams and starts the NotificationClientThread.
   */
  private void connect() throws Exception {
    serverInterface = this.retrieveServer();
    notiClientRunnable = new NotiClientRunnable(this.serverInterface);
    messageSender = Sender.create();
    Thread thread = new Thread(messageReceiver);
    thread.start();
    if (messageSender == null) throw new IOException();
  }

  /**
   * Print help message.
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
                    "  receive: to retrieve all the unread com.distributed.chat messages\n" +
                    "  send TEXT: to send the TEXT message regarding the document being edited";
    System.out.println(message);
  }

  /**
   * Loop for input commands, interprets and executes the command.
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
          ex.printStackTrace();
          System.err.println("Unsupported arguments. Please try again.");
        } catch (Exception e) {
          e.printStackTrace();
          System.err.println("Internal error. Please try again.");
        }
      }
    }
  }

  /**
   * Retrieve an available server from central com.distributed.server.
   *
   * @return assigned server
   */
  private ServerInterface retrieveServer() {
    try {
      Registry centralRegistry = LocateRegistry.getRegistry(CENTRAL_SERVER_HOST, CENTRAL_SERVER_RMI_PORT);
      CentralServerInterface central = (CentralServerInterface) centralRegistry.lookup(CentralServer.class.getSimpleName() + CENTRAL_SERVER_RMI_PORT);
      int serverPort = central.assignAliveServerToClient();
      System.out.println("Assigned to " + serverPort);

      Registry registry = LocateRegistry.getRegistry(serverPort);
      ServerInterface serverInterface = (ServerInterface) registry.lookup(Server.class.getSimpleName() + serverPort);
      return serverInterface;
    } catch (Exception e) {
      throw new RuntimeException("Unable to retrieve server");
    }
  }


  /**
   * Register a new user.
   */
  private void register(String username, String password) throws Exception {
    Result result = serverInterface.createUser(new User(username, password));
    if (result.getStatus() == 1) {
      System.out.println("User " + username + " registered successfully!");
    } else {
      System.err.println(result.getMessage());
    }
  }

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
   * Kill session, stop NotificationClientThread and clears notifications.
   */
  private void logout() throws Exception {
    if (session != null) {
      if (!session.isEditing()) {
        Result result = serverInterface.logout(new User(session.getUser().getUsername()));
        if (result.getStatus() == 0) {
          System.err.println(result.getMessage());
          return;
        }
        session = null;
        notiClientRunnable.clearNotificationList();
        notiClientRunnable.setUser(null);
        notiClientRunnable.stop();
        System.out.println("Successfully logged out.");
      } else System.err.println("You should 'endedit' before logging out");
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
      Result result = serverInterface.createDocument(user, request);
      if (result.getStatus() == 0) {
        System.err.println(result.getMessage());
        return;
      }
      System.out.println("Successfully create a new document.");
    } else System.err.println("You're not logged in");
  }

  /**
   * Start an edit session for a document. Retrieve file stream from server and write in on local
   * file. Initialize a new multicast group for a {@code MessageReceiver}.
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
        request.setToken(session.getSessionToken());
        Result result = serverInterface.edit(new User(session.getUser().getUsername()), request);
        if (result.getStatus() == 0) {
          System.err.println(result.getMessage());
          return;
        }
        InputStream inputStream = RemoteInputStreamClient.wrap(result.getRemoteInputStream());
        fileStream.write(inputStream.read());

        session.setOccupiedFilePath(filepath);
        session.setOccupiedFileName(docName);
        session.setSectionIndex(secNumber);
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
   * Stops editing the current section and leave multicast group.
   */
  private void editEnd() {
    if (session != null) {
      if (session.isEditing()) {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(session.getOccupiedFilePath()), StandardOpenOption.READ);
             InputStream stream = Channels.newInputStream(fileChannel)) {
          RemoteInputStreamServer remoteFileData = new SimpleRemoteInputStream(stream);
          Request request = new Request();
          request.setDocName(session.getOccupiedFileName());
          request.setSectionNum(session.getSectionIndex());
          request.setRemoteInputStream(remoteFileData);
          request.setToken(session.getSessionToken());
          serverInterface.editEnd(new User(session.getUser().getUsername()), request);
          session.setOccupiedFilePath(null);
          session.setOccupiedFileName(null);
          session.setSectionIndex(0);
          messageReceiver.setNewGroup(0);
        } catch (Exception ex) {
          printException(ex);
        }
      } else System.err.println("You're not editing any section");
    } else System.err.println("You're not logged in");
  }

  /**
   * Read content of requested section.
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
        if (result.getStatus() == 1) {
          if (!editor.equals("None")) {
            System.out.println(String.format("%s is editing the section right now", editor));
          } else System.out.println("None is editing this section");
        }
      } catch (IOException ex) {
        ex.printStackTrace();
        printException(ex);
      }
    } else System.err.println("You're not logged in");
  }

  /**
   * Gets the list of documents the user has permissions.
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
   * Shares a document and read/write permission with another user. The shared user will also
   * receive a notification.
   */
  private void share(String user, String docName) throws Exception {
    Request request = new Request();
    request.setToken(session.getSessionToken());
    request.setTargetUser(new User(user));
    request.setDocName(docName);
    serverInterface.shareDoc(new User(session.getUser().getUsername()), request);
  }

  /**
   * Read the requested document and concatenate all its sections.
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
   * Print all new notifications collected since the last printing.
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
   * Shows all new received messages.
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
   * Send a new UDP multicast packet to every listening receiver
   */
  private void sendMessage(String text) {
    if (session != null) {
      if (session.isEditing()) {
        InetAddress groupAddress;
        if ((groupAddress = messageReceiver.getActiveGroup()) != null) {
          try {
            Message message = new Message(session.getUser().getUsername(), text, System.currentTimeMillis());
            messageSender.sendMessage(message, new InetSocketAddress(groupAddress, UDP_PORT));
          } catch (Exception ex) {
            printException(ex);
          }
        } else System.err.println("Generic message sending error");
      } else System.err.println("You're not editing any document");
    } else System.err.println("You're not logged in");
  }

  /**
   * Print easy to read exception.
   */
  private void printException(Exception ex) {
    System.err.println(ex.getMessage());
  }


}
