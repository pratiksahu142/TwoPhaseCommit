import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a coordinator in two phase commit protocol which polls the slave servers to check if
 * write operation can be done consistently else aborts.
 */
public interface Coordinator extends Remote {

  /**
   * Adds a follower/slave server to the list of followers.
   *
   * @param follower slave server
   * @throws RemoteException when an exception occurs in the remote
   */
  void addFollower(KeyValue follower) throws RemoteException;

  /**
   * Polls all the followers if server can give the locks to coordinator.
   *
   * @param operation to be performed on servers
   * @return true if all coordinators are able to lock the server for the operation
   * @throws RemoteException when an exception occurs in the remote
   */
  boolean prepareFollower(String operation) throws RemoteException;

  /**
   * Performs commit on servers after obtaining the locks and release locks once operation
   * completes.
   *
   * @param operation to be performed on servers
   * @throws RemoteException when an exception occurs in the remote
   */
  void commitOnFollowers(String operation) throws RemoteException;

  /**
   * Abort the operation on servers and release the locks if had acquired previously.
   *
   * @param operation to be performed on servers
   * @throws RemoteException when an exception occurs in the remote
   */
  void abortInFollowers(String operation) throws RemoteException;

}
