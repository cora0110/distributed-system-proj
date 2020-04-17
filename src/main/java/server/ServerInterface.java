package server;

import model.*;

import java.io.FileInputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ServerInterface extends Remote {

    Result prepare(UUID transactionID) throws RemoteException;

    PrepareAck participantsPrepare(UUID transactionID) throws RemoteException;

    Result accept(UUID transactionID) throws RemoteException;

    AcceptAck participantsAccept(UUID transactionID, CommitParams commitParams) throws RemoteException;

    Result commit(UUID transactionID, CommitParams commitParams) throws RemoteException;

    Result initDocument(String docName, User user) throws RemoteException;
    Result initSection(int sectionNum, User user) throws RemoteException;

    Result createUser(User user) throws RemoteException;

    Result login(User user) throws RemoteException;
    Result logout(User user) throws RemoteException;

    /**
     *
     * @param
     * @return
     * @throws RemoteException
     * @see "https://openhms.sourceforge.io/rmiio/"
     */
    Result edit(User user, Request request) throws RemoteException;
    Result editEnd(User user, FileInputStream fileInputStream) throws RemoteException;

    Result createDocument(User user, Request request) throws RemoteException;

    Result showSection(User user, Request request) throws RemoteException;
    Result showDocumentContent(User user, Request request) throws RemoteException;

    Result listOwnedDocs(User user) throws RemoteException;

    // TODO: Since we are unfamiliar with the notification mechanism, please feel free to change/add the signatures
    Result shareDoc(User user, Request request) throws RemoteException;

    void kill() throws RemoteException;

    boolean restart( DocumentDatabase documentDatabase,
                         AliveUserDatabase aliveUserDatabase,
                         UserDatabase userDatabase) throws  RemoteException;

    boolean helpRestartServer(int deadServerPort) throws  RemoteException;

}
