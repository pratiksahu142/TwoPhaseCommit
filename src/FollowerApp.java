import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Represents an entry point for creating a follower server and registering it in RMI registry.
 */
public class FollowerApp {

  private static final String RMI_LOCALHOST_CONNECTION_URL = "rmi://localhost:";

  public static void main(String[] args) throws IllegalArgumentException {
    if (args == null || args.length != 2) {
      throw new IllegalArgumentException("usage: java FollowerApp <coordinator-port> <server-port>");
    }
    startServers(args);
  }

  private static void startServers(String[] args) {
    try {
      int leaderPort = Integer.parseInt(args[0]);
      int serverPort = Integer.parseInt(args[1]);

      KeyValue follower = new KeyValueStore();

      Coordinator leader = (Coordinator) Naming.lookup(
          RMI_LOCALHOST_CONNECTION_URL + leaderPort + "/leader");
      leader.addFollower(follower);
      follower.setCoordinator(leader);

      LocateRegistry.createRegistry(serverPort);
      String followerAddress = RMI_LOCALHOST_CONNECTION_URL + serverPort + "/keyValueStore";
      Naming.rebind(followerAddress, follower);
      System.out.println("Server started on: " + followerAddress);

    } catch (NumberFormatException ne) {
      throw new IllegalArgumentException("Invalid Port numbers");
    } catch (RemoteException | MalformedURLException | NotBoundException e) {
      throw new RuntimeException(e);
    }
  }
}
