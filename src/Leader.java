import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a coordinator in two phase commit protocol that coordinates among slave servers to
 * fulfil client requests maintaining a consistent view of key-value store.
 */
public class Leader extends UnicastRemoteObject implements Coordinator {

  private final Logger logger = Logger.getLogger(Leader.class.getName());
  private final List<KeyValue> followers;
  private int voteCount;

  /**
   * Constructs an instance of coordinator server.
   * @throws RemoteException when an exception occurs in the remote
   */
  protected Leader() throws RemoteException {
    this.followers = new ArrayList<>();
    this.voteCount = 0;
  }

  @Override
  public void addFollower(KeyValue follower) throws RemoteException {
    this.followers.add(follower);
  }

  @Override
  public synchronized boolean prepareFollower(String operation) throws RemoteException {
    this.voteCount = 0;
    logger.info("Polling followers for operation: " + operation);
    for (KeyValue follower : followers) {
      try {
        if (follower.prepare(operation)) {
          this.voteCount++;
        }
      } catch (Exception e) {
        logger.warning("Something went wrong when preparing follower!");
      }
    }
    logger.info("Voting result is: " + this.voteCount + "/" + followers.size());
    return this.voteCount == followers.size();
  }

  @Override
  public void commitOnFollowers(String operation) throws RemoteException {
    for (KeyValue follower : followers) {
      try {
        boolean isCommitted = follower.commit(operation);
        logger.info("Committed: " + isCommitted);
      } catch (Exception e) {
        logger.warning("Something went wrong when committing on follower!");
      }
    }
  }

  @Override
  public void abortInFollowers(String operation) throws RemoteException {
    for (KeyValue follower : followers) {
      try {
        boolean isAborted = follower.abort(operation);
        logger.info("Aborted: " + isAborted);
      } catch (Exception e) {
        logger.warning("Something went wrong when aborting on follower!");
      }
    }
  }
}
