import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.Logger;

/**
 * Represents an entry point for creating a coordinator server and registering it in RMI registry.
 */
public class LeaderApp {

  private static final String RMI_LOCALHOST_CONNECTION_URL = "rmi://localhost:";
  private static final Logger logger = Logger.getLogger(LeaderApp.class.getName());

  public static void main(String[] args) throws IllegalArgumentException {
    if (args == null || args.length != 1) {
      throw new IllegalArgumentException("usage: java LeaderApp <coordinator-port-number>");
    }
    startCoordinator(args[0]);
  }

  private static void startCoordinator(String arg) {
    try {
      int leaderPort = Integer.parseInt(arg);
      Coordinator leader = new Leader();
      LocateRegistry.createRegistry(leaderPort);
      String leaderAddress = RMI_LOCALHOST_CONNECTION_URL + leaderPort + "/leader";
      Naming.rebind(leaderAddress, leader);
      logger.info("Leader started on: " + leaderAddress);
    } catch (RemoteException | MalformedURLException e) {
      logger.severe("Leader could not be started due to: " + e.getMessage());
    } catch (NumberFormatException ne) {
      logger.warning("Invalid port number!");
    }
  }
}
