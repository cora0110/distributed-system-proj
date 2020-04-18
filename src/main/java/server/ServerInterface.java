package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

import model.AcceptAck;
import model.CommitParams;
import model.PrepareAck;
import model.Request;
import model.Result;
import model.User;

public interface ServerInterface extends Remote {

  boolean prepare(UUID transaction, CommitParams commitParams) throws RemoteException;

  boolean receivePrepare(UUID transaction, CommitParams commitParams) throws RemoteException;

  boolean commitOrAbort(UUID transaction) throws RemoteException;

  void receiveCommit(UUID transaction) throws RemoteException;

  void receiveAbort(UUID transaction) throws RemoteException;

  void executeCommit(CommitParams commitParams) throws RemoteException;

  Result createUser(User user) throws RemoteException;

  Result login(User user) throws RemoteException;

  Result logout(User user) throws RemoteException;

  /**
   * @param
   * @return
   * @throws RemoteException
   * @see "https://openhms.sourceforge.io/rmiio/"
   */
  Result edit(User user, Request request) throws RemoteException;

  Result editEnd(User user, Request request) throws RemoteException;

  Result createDocument(User user, Request request) throws RemoteException;

  Result showSection(User user, Request request) throws RemoteException;

  Result showDocumentContent(User user, Request request) throws RemoteException;

  Result listOwnedDocs(User user, Request request) throws RemoteException;

  // TODO: Since we are unfamiliar with the notification mechanism, please feel free to change/add the signatures
  Result shareDoc(User user, Request request) throws RemoteException;

  Result getNotifications(User user) throws RemoteException;

  void kill() throws RemoteException;

  boolean restart(DocumentDatabase documentDatabase,
                  AliveUserDatabase aliveUserDatabase,
                  UserDatabase userDatabase) throws RemoteException;

  boolean helpRestartServer(int deadServerPort) throws RemoteException;


}
