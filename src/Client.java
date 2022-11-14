import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.AccessException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a Remote client in simple Java RMI program.
 */
public class Client {

  public static final Logger logger = Logger.getLogger(Client.class.getName());
  private static final String RMI_LOCALHOST_CONNECTION_URL = "rmi://localhost:";
  private static final String SERVICE_NAME = "/keyValueStore";
  private static final String EXIT = "exit";
  private static final String REQUEST_FOR_INPUT =
      "What operation do you want to do?"
          + "\nPUT:<key>:<value>"
          + "\nGET:<key>"
          + "\nDELETE:<key>"
          + "\nType 'change' to connect to a different server\n";

  private static final String PUT = "PUT";
  private static final String GET = "GET";
  private static final String DELETE = "DELETE";
  private static final String DELIMITER_COLON = ":";
  private static final String MALFORMED = "malformed";

  /**
   * Main method to instantiate and run the client.
   *
   * @param args any parameters for driver of the client
   */
  public static void main(String[] args) throws IllegalArgumentException {
    if (args == null || args.length != 1) {
      throw new IllegalArgumentException("usage: java Client <server-port>");
    }
    Client client = new Client();
    int port = Integer.parseInt(args[0]);
    try {
      KeyValue keyValue = (KeyValue) Naming.lookup(
          RMI_LOCALHOST_CONNECTION_URL + port + SERVICE_NAME);
      Scanner sc = new Scanner(System.in);
      client.performInitialOperationsOnClient(client, keyValue, sc);
      handleUserRequests(client, keyValue, sc);
    } catch (MalformedURLException murle) {
      logger.warning("MalformedURLException: " + murle.getMessage());
    } catch (AccessException ae) {
      logger.warning("AccessException: " + ae.getMessage());
    } catch (RemoteException re) {
      logger.warning("RemoteException: " + re.getMessage());
    } catch (NotBoundException nbe) {
      logger.warning("NotBoundException: " + nbe.getMessage());
    } catch (NoSuchElementException nee) {
      logger.warning("NoSuchElementException: " + nee.getMessage());
    } catch (IllegalStateException ise) {
      logger.warning("IllegalStateException: " + ise.getMessage());
    } catch (Exception e) {
      logger.warning("Exception: " + e.getMessage());
    }
  }

  private static void handleUserRequests(Client client, KeyValue keyValue, Scanner sc)
      throws RemoteException {
    try {
      while (true) {
        logger.info(REQUEST_FOR_INPUT);
        String operation = sc.nextLine();
        if (operation.equalsIgnoreCase(EXIT)) {
          break;
        }
        if (operation.equalsIgnoreCase("change")) {
          System.out.println("Enter a valid port number for server:");
          try {
            int newPort = Integer.parseInt(sc.nextLine().trim());
            keyValue = (KeyValue) Naming.lookup(
                RMI_LOCALHOST_CONNECTION_URL + newPort + SERVICE_NAME);
            logger.info("Connected to another server on: "+newPort);
          } catch (Exception e) {
            logger.info("Entered input was incorrect, please try again!");
          }
          continue;
        } else if (client.isInvalidInput(operation)) {
          logger.info("Entered input was incorrect, please try again!");
          continue;
        }
        String reply = client.performOperation(operation, keyValue);
        logger.info(reply);
      }
    } catch (NoSuchElementException nee) {
      logger.warning("NoSuchElementException: " + nee.getMessage());
    } catch (IllegalStateException ise) {
      logger.warning("IllegalStateException: " + ise.getMessage());
    } catch (NumberFormatException nfe) {
      logger.warning("NumberFormatException: " + nfe.getMessage());
    }
  }

  private String performOperation(String request, KeyValue keyValue) throws RemoteException {
    String[] reqArr;
    try {
      reqArr = request.split(DELIMITER_COLON);
      if (reqArr[0].equalsIgnoreCase(PUT) && reqArr.length == 3) {
        String key = reqArr[1];
        String value = reqArr[2];
        return keyValue.put(key, value);
      } else if (reqArr[0].equalsIgnoreCase(GET) && reqArr.length == 2) {
        String key = reqArr[1];
        return keyValue.get(key);
      } else if (reqArr[0].equalsIgnoreCase(DELETE) && reqArr.length == 2) {
        String key = reqArr[1];
        return keyValue.delete(key);
      }
    } catch (PatternSyntaxException pse) {
      logger.warning("PatternSyntaxException: " + pse.getMessage());
    } catch (RemoteException re) {
      logger.warning("RemoteException: " + re.getMessage());
    }

    return MALFORMED;
  }

  private boolean isInvalidInput(String operation) {
    boolean result = false;
    try {
      result = operation.matches("PUT:(.*)") || operation.matches("GET:(.*)") || operation.matches(
          "DELETE:(.*)") || operation.equalsIgnoreCase(EXIT) || operation.contains("wait");

    } catch (PatternSyntaxException pse) {
      logger.warning("PatternSyntaxException: " + pse.getMessage());
    }
    return !result;
  }

  private void performInitialOperationsOnClient(Client client, KeyValue keyValue, Scanner sc) {
    System.out.println("Do you want to perform initial operations?");
    String toPerform = sc.nextLine();
    if (toPerform.equalsIgnoreCase("yes")) {
      System.out.println("Performing Initial operations and loading some data");
      try {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
        String line;
        while ((line = reader.readLine()) != null) {
          if (client.isInvalidInput(line)) {
            logger.info("Entered input was incorrect, please try again!");
            continue;
          }
          client.performOperation(line, keyValue);
        }
      } catch (IOException e) {
        logger.warning("Initial operations could not be performed due to: " + e.getMessage());
      }
    }
  }
}
