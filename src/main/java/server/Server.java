package server;

import model.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server implements ServerInterface {
    public int port;
    private DocumentDatabase documentDatabase;
    private AliveUserDatabase aliveUserDatabase;
    private UserDatabase userDatabase;
    private ReentrantReadWriteLock readWriteLock;

    private NotificationServerThread notificationThread;

    private final String DATA_DIR = "./server_data" + port + "/";

    public Server(int port) {
        this.port = port;
        userDatabase = initUserDB();
        documentDatabase = initDocumentDB();
        aliveUserDatabase = new AliveUserDatabase();
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
            else return new Result(0, "Unknown failure");
        } else {
            return new Result(0, "Username already exists");
        }
    }

    @Override
    public Result login(User user) throws RemoteException {
        if (aliveUserDatabase.isLoggedIn(user.getUsername())) {
            return new Result(0, "Already logged in.");
        } else {
            User loggedInUser = userDatabase.doLogin(user.getUsername(), user.getPassword());
            if (loggedInUser != null) {
                CommitParams commitParams =
                        new CommitParams(user, 1, null, null, -1, 1);
                Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);
                String token = result.message;
                if (token != null) {
                    // TODO
                    notificationThread = new NotificationServerThread(user, socket.getInetAddress().getHostName(), (Integer) args[2]);
                    notificationThread.start();
                    System.out.println("New user logged in: " + user.getUsername());
                    return new Result(1, "succeed");
                } else {
                    return new Result(0, "Token generation failure while logging in.");
                }
            } else {
                return new Result((0, "Username and password do not match."));
            }
        }
    }

    @Override
    public Result logout(User user) throws RemoteException {
        // TODO
        notificationThread.close();

        CommitParams commitParams =
                new CommitParams(user, 0, null, null, -1, 1);
        Result result = twoPhaseCommit(UUID.randomUUID(), commitParams);

        if (result.getStatus() == 1) {
            System.out.println("User logged out: " + user.getUsername());
            return new Result(1, "succeed");
        } else {
            return new Result(0, "Failure while removing from alive user DB.");
        }

    }

    @Override
    public Result edit(User user, Request request) throws RemoteException {
        return null;
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

        documentDatabase.createNewDocument(DATA_DIR, request.getSectionNum(), request.getDocName(), user);
        return new Result(1, "Succeed");
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

        if (!document.canAccess(user)) {
            return new Result(0, "You do not have access.");
        }

        Section section = document.getSection(request.getSectionNum());
        if (section == null) {
            return new Result(0, "Section does not exist.");

        }

        User editingUser = section.getUserOnEditing();
        if (editingUser == null) {
            return new Result(1, "None");
        }
        return new Result(1, editingUser.getUsername());
    }

    @Override
    public Result showDocumentContent(User user, Request request) throws RemoteException {
        return null;
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

        if (!document.isCreator(user)) {
            return new Result(0, "You do not have access.");
        }

        document.addModifier(request.getTargetUser());
        return new Result(1, "Succeed");
    }

    private UserDatabase initUserDB() {
        UserDatabase loadedUsersDB = loadUserDB();
        return loadedUsersDB == null ? new UserDatabase() : loadedUsersDB;
    }

    private UserDatabase loadUserDB() {
        try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(DATA_DIR + "UserDB.dat"))) {
            return (UserDatabase) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return  null;
        }
    }

    private DocumentDatabase initDocumentDB() {
        DocumentDatabase loadedDocumentsDB = loadDocumentDB();
        return loadedDocumentsDB == null ? new DocumentDatabase() : loadedDocumentsDB;
    }


    private DocumentDatabase loadDocumentDB() {
        try(ObjectInputStream input = new ObjectInputStream(new FileInputStream(DATA_DIR + "DocDB.dat"))) {
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
