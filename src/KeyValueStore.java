import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Represents a server serving as a distributed key-value store.
 */
public class KeyValueStore extends UnicastRemoteObject implements KeyValue {

  public static final String SUCCESSFUL = " successful!";
  public static final String FAILED = " failed!";
  public static final String PUT = "PUT";
  public static final String DELETE = "DELETE";
  public static final String COLON = ":";
  private final Logger logger = Logger.getLogger(KeyValueStore.class.getName());
  private final HashMap<String, String> keyValueStore;
  private final ReentrantLock lock;
  public String DELETED_VALUE;
  private Coordinator leader;

  /**
   * Constructs an instance of server.
   *
   * @throws RemoteException when an exception occurs in the remote
   */
  protected KeyValueStore() throws RemoteException {
    keyValueStore = new HashMap<>();
    lock = new ReentrantLock(true);

  }

  @Override
  public boolean prepare(String operation) throws RemoteException {
    return lock.tryLock();
  }

  @Override
  public boolean commit(String operation) throws RemoteException {
    boolean success = true;

    try {
      Thread.sleep(1000);
      String[] operands = operation.split(COLON);
      if (operands[0].equalsIgnoreCase(PUT)) {
        this.putValue(operands[1], operands[2]);
      } else if (operands[0].equalsIgnoreCase(DELETE)) {
        this.deleteKeyValue(operands[1]);
      }
    } catch (Exception e) {
      success = false;
      return success;
    } finally {
      try {
        lock.unlock();
      } catch (IllegalMonitorStateException imse) {
        logger.warning("Could not unlock while commit!");
      }
    }
    return success;
  }

  @Override
  public boolean abort(String operation) throws RemoteException {
    boolean success = true;
    try {
      lock.unlock();
    } catch (IllegalMonitorStateException imse) {
      logger.warning("Could not unlock while abort!");
    }
    return success;
  }

  @Override
  public String get(String key) throws RemoteException {
    String value = "GET" + FAILED;
    logger.info("Getting value for key " + key);
    try {
      if (lock.tryLock(10, TimeUnit.SECONDS)) {
        value = keyValueStore.get(key);
        if (value == null) {
          logger.warning("Could not find key :\"" + key + "\".");
        } else {
          logger.info("Fetched the value \"" + value + "\" for the key \"" + key + "\"");
        }
        return value;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        lock.unlock();
      } catch (IllegalMonitorStateException imse) {
        logger.warning("Could not unlock while get!");
      }
    }
    return value;
  }

  @Override
  public String put(String key, String value) throws RemoteException {
    String operation = PUT + COLON + key + COLON + value;
    if (this.leader.prepareFollower(operation)) {
      this.leader.commitOnFollowers(operation);
      return operation + SUCCESSFUL;
    } else {
      this.leader.abortInFollowers(operation);
      return operation + FAILED;
    }
  }

  private void putValue(String key, String value) throws RemoteException {
    String status = null;
    logger.info("Putting " + key + ":" + value);
    status = keyValueStore.put(key, value);
    if (status == null) {
      logger.info("Value :\"" + value + "\" inserted for key :\"" + key + "\".");
    } else {
      logger.info("Value :\"" + value + "\" overriden for key :\"" + key + "\".");
    }
  }

  @Override
  public String delete(String key) throws RemoteException {
    String operation = DELETE + COLON + key;
    if (this.leader.prepareFollower(operation)) {
      this.leader.commitOnFollowers(operation);
      return operation + SUCCESSFUL;
    } else {
      this.leader.abortInFollowers(operation);
      return operation + FAILED;
    }
  }

  private void deleteKeyValue(String key) throws RemoteException {
    String status = null;
    status = keyValueStore.remove(key);
    logger.info("Deleting key " + key);
    if (status != null) {
      logger.info("Entry with key :\"" + key + "\" deleted.");
    } else {
      logger.warning("Could not find key :\"" + key + "\" to delete.");
    }
    DELETED_VALUE = status;
  }

  @Override
  public void setCoordinator(Coordinator coordinator) throws RemoteException {
    this.leader = coordinator;
  }
}
