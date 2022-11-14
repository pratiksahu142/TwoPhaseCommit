import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a key-value store that stores pair of unique strings as a pair.
 */
public interface KeyValue extends Remote {

  /**
   * @param operation to be performed on key-value store
   * @return true if able to obtain lock
   * @throws RemoteException
   */
  boolean prepare(String operation) throws RemoteException;

  /**
   * @param operation to be performed on key-value store
   * @return true if able to commit the operation
   * @throws RemoteException when an exception occurs in the remote
   */
  boolean commit(String operation) throws RemoteException;

  /**
   * Abort the operation and release locks.
   *
   * @param operation to be performed on key-value store
   * @return true if successfully release locks
   * @throws RemoteException when an exception occurs in the remote
   */
  boolean abort(String operation) throws RemoteException;

  /**
   * Returns the value to which the specified key is stored, or appropriate message if this store
   * contains no mapping for the key.
   *
   * @param key of the pair
   * @return value mapped for the key
   * @throws RemoteException when an exception occurs in the remote
   */
  String get(String key) throws RemoteException;

  /**
   * Inserts the given value with the given key in this key-value store. If the store previously
   * contained a mapping for the key, the old value is replaced.
   *
   * @param key   of the pair
   * @param value of the pair
   * @return null if pair is inserted first time for the key, else returns previous value for the
   * same key from store
   * @throws RemoteException when an exception occurs in the remote
   */
  String put(String key, String value) throws RemoteException;

  /**
   * Removes the mapping for the specified key from this map if present.
   *
   * @param key key of the pair
   * @return appropriate message if pair is successfully deleted or not
   * @throws RemoteException when an exception occurs in the remote
   */
  String delete(String key) throws RemoteException;

  /**
   * Sets the coordinator server that helps perform put and delete operations on all live servers.
   *
   * @param coordinator leader server
   * @throws RemoteException when an exception occurs in the remote
   */
  void setCoordinator(Coordinator coordinator) throws RemoteException;

}
