package com.distributed.server;

import com.distributed.model.BackupData;
import com.distributed.model.CommitParams;
import com.distributed.model.Request;
import com.distributed.model.Result;
import com.distributed.model.User;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ServerInterface extends Remote {

  boolean prepare(UUID transactionID, CommitParams commitParams) throws RemoteException;

  boolean receivePrepare(UUID transactionID, CommitParams commitParams) throws RemoteException;

  void commitOrAbort(UUID transactionID, boolean ack) throws RemoteException;

  boolean receiveCommit(UUID transactionID) throws RemoteException;

  boolean receiveAbort(UUID transactionID) throws RemoteException;

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

  Result shareDoc(User user, Request request) throws RemoteException;

  Result getNotifications(User user) throws RemoteException;

  void kill() throws RemoteException;

  boolean recoverData(BackupData backupData) throws RemoteException;

  boolean helpRecoverData(int targetPort) throws RemoteException;
}
