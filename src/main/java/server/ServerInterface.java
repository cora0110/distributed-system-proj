package main.java.server;

import main.java.model.AcceptAck;
import main.java.model.PrepareAck;
import main.java.model.Result;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

public interface ServerInterface extends Remote {

    Result prepare(UUID transactionID) throws RemoteException;

    PrepareAck participantsPrepare(UUID transactionID) throws RemoteException;

    Result accept(UUID transactionID, String request) throws RemoteException;

    AcceptAck participantsAccept(UUID transactionID, String request) throws RemoteException;

    Result requestHandler(String args) throws RemoteException;

    Result onLogin(String args) throws RemoteException;
    Result onLogout(String args) throws RemoteException;

    /**
     *
     * @param args
     * @return
     * @throws RemoteException
     * @see "https://openhms.sourceforge.io/rmiio/"
     */
    Result onEdit(String args) throws RemoteException;
    Result onEditEnd(String args) throws RemoteException;

    Result onCreate(String args) throws RemoteException;

    Result onShowSection(String args) throws RemoteException;
    Result onShowDocument(String args) throws RemoteException;

    Result onList(String args) throws RemoteException;

    // TODO: Since we are unfamiliar with the notification mechanism, please feel free to change/add the signatures
    Result onShare(String args) throws RemoteException;
}
