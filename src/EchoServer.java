
import java.io.File;  // Import the File class
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EchoServer extends AbstractServer {
    //Class variables *************************************************

    /**
     * The default port to listen on.
     */
    //final public static int DEFAULT_PORT = 5555;
    //Constructors ****************************************************
    /**
     * Constructs an instance of the echo server.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {

        super(port);

        try {
            //this.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }

    }

    //constructor with chatIF arguement to take in "this"
    public EchoServer(int port, ChatIF server) {

        super(port);

        try {
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }
    }

    FileWriter myWriter;

    //Instance methods ************************************************
    /**
     * This method handles any messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        if (msg instanceof Envelope) {
            handleCommandFromClient(msg, client);
        } else {
            System.out.println("Message received: '" + msg + "' from " + client);
            String userName = client.getInfo("userid").toString();
          
            this.sendToAllClientsInRoom(userName + ": " + msg, client);
        }
    }

    public void handleMessageFromClientRecord(Object msg, ConnectionToClient client) {
        if (msg instanceof Envelope) {
            handleCommandFromClient(msg, client);
        } else {
            System.out.println("Message received: " + msg + " from " + client);
            String userName = client.getInfo("userid").toString();

            this.sendToAllClientsInRoom(userName + ": " + msg, client);
        }
    }

    public void handleCommandFromClient(Object msg, ConnectionToClient client) {
        Envelope env = (Envelope) msg;
        String id = env.getId();

        if (id.equals("login")) {
            String userName = env.getContent().toString();
            client.setInfo("userid", userName);
            client.setInfo("room", "lobby");
        }
        if (id.equals("join")) {
            String room = client.getInfo("room").toString();
        String clientId = client.getInfo("userid").toString();
          
        //Checking to see if the client is recording or not
            if (client.getInfo("disable").equals("No")) {
                try {
                    client.sendToClient("You cannot change rooms while recording");
                } catch (IOException ex) {
                    System.out.println(" Error changing rooms");
                }
            } else {
                room = env.getContent().toString();
                client.setInfo("room", room);
                
               Envelope  EnableEnv = new Envelope("enable", "", "");
                try {
                    client.sendToClient(EnableEnv);
                           client.sendToClient("WARNING!! You will not receive a copy of the recording");
                } catch (IOException ex) {
                    System.out.println("Error enabling back the button");
                }
                       // client.setInfo("disable", "No");
                
            }
           
        }
        if (id.equals("pm")) {
            String recipient = env.getArg();
            Object message = env.getContent();
            sendToAClient(message, recipient, client);
        }
        if (id.equals("yell")) {
            Object message = env.getContent();
            this.sendToAllClients(client.getInfo("userid") + "<yelling>: " + message);
        }
        if (id.equals("who")) {
            sendUserListToClient(client);
        }
        //When record button is pressed
        if (id.equals("record")) {
            Object message = env.getContent();
            if (client.getInfo("disable") == null || client.getInfo("disable").equals("No")) {
                sendToAllClientsInRoom("The Room is being recorded", client);

                // disable Record Button
                changeRecordToDisable(client);
                
                try {
                    //Cleaning the file
                    myWriter = new FileWriter("filename.txt");
                    myWriter.close();

                    //Opening fileWriter
                    myWriter = new FileWriter("filename.txt", true);

                    //GUIConsole g = new GUIConsole();
                    // g.recordB.setEnabled(false);
                } catch (IOException ex) {
                    System.out.println("Error creating fileWriter");;
                }
            } else if (client.getInfo("disable").equals("Yes")) {
                System.out.println("Your Record Button is disabled");
            }

        }
        //When recording is stopped
        if (id.equals("stopRecord")) {
            Object message = env.getContent();
            {
                try {
                    //Closing fileWriter
                
                    myWriter.close();
                    //create envelope holding the file and send to all clients
                    File file = new File("filename.txt");
                    byte[] data = Files.readAllBytes(file.toPath());
                    // System.out.println("Successfully wrote to the file.");

                    Envelope fileEnv = new Envelope();
                    fileEnv.setId("transcript");

                    // byte[] bytes = Files.readAllBytes(Paths.get(filePath));
                    
                    //Getting the time and date
                    String args = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

                    //making file name
                    String room = client.getInfo("room").toString();
                    String Arg = args + room;
                    fileEnv.setArg(Arg);
                    fileEnv.setContent(data);

                    sendToAllClientsInRoom(fileEnv, client);

                    sendToAllClientsInRoom("A copy of transcript has been sent to you.", client);

                   
                    
                } catch (IOException ex) {
                    System.out.println("Error creating fileWriter");;
                }
                //Enabling everyone's buttons back
                 Envelope EnableEnv = new Envelope("enable", "", "");
                 sendToAllClientsInRoom(EnableEnv, client);
                 client.setInfo("disable", "No");
                          
            }
            
        }

    }

    public void sendUserListToClient(ConnectionToClient client) {
        ArrayList<String> userList = new ArrayList<String>();
        String room = client.getInfo("room").toString();

        Thread[] clientThreadList = getClientConnections();

        for (int i = 0; i < clientThreadList.length; i++) {
            try {
                ConnectionToClient recipient = ((ConnectionToClient) clientThreadList[i]);
                if (recipient.getInfo("room").equals(room)) {
                    userList.add(recipient.getInfo("userid").toString());
                }
            } catch (Exception ex) {
                System.out.println("Unable to send a client");
            }
        }
        Envelope env = new Envelope("who", room, userList);
        try {
            client.sendToClient(env);

        } catch (Exception e) {
            System.out.println("Failed to send client list");
        }
    }

    public void sendToAClient(Object msg, String recipient, ConnectionToClient client) {
        String sender = client.getInfo("userid").toString();

        System.out.println("--------Sender: " + sender);
        System.out.println("--------Recipient: " + recipient);
        System.out.println("--------Message: " + msg);

        Thread[] clientThreadList = getClientConnections();

        for (int i = 0; i < clientThreadList.length; i++) {
            try {
                ConnectionToClient clientConn = ((ConnectionToClient) clientThreadList[i]);
                if (clientConn.getInfo("userid").equals(recipient)) {
                    clientConn.sendToClient(sender + "<private>: " + msg);
                }

            } catch (Exception ex) {
                System.out.println("Unable to send a client");
            }
        }

    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * starts listening for connections.
     */
    public void sendToAllClientsInRoom(Object msg, ConnectionToClient client) {

        String room = client.getInfo("room").toString();

        Thread[] clientThreadList = getClientConnections();

        //Writing to file if fileWriter has been opened
        if (this.myWriter != null) {
            try {
                //write to file here
                myWriter.write(msg.toString());
                myWriter.write("\n");
            } catch (IOException ex) {
                System.out.println("Error writing to file");
            }
        }
        for (int i = 0; i < clientThreadList.length; i++) {
            try {
                ConnectionToClient recipient = ((ConnectionToClient) clientThreadList[i]);
                if (recipient.getInfo("room").equals(room)) {
                    recipient.sendToClient(msg);
                }
                //((ConnectionToClient) clientThreadList[i]).sendToClient(msg);
            } catch (Exception ex) {
                System.out.println("Unable to send a client");
            }

        }
    }

    protected void serverStarted() {
        System.out.println("Server listening for connections on port " + getPort());
    }

    /**
     * This method overrides the one in the superclass. Called when the server
     * stops listening for connections.
     */
    protected void serverStopped() {
        System.out.println("Server has stopped listening for connections.");
    }

    //Class methods ***************************************************
    /**
     * This method is responsible for the creation of the server instance (there
     * is no UI in this phase).
     *
     * @param args[0] The port number to listen on. Defaults to 5555 if no
     * argument is entered.
     */
    public static void main(String[] args) {
        int port = 0; //Port to listen on

        try {
            port = Integer.parseInt(args[0]);

        } catch (IndexOutOfBoundsException ioobe) {
            port = 5555;
        }

        try {
            File myObj = new File("filename.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        // port = DEFAULT_PORT; //Set port to 5555
        EchoServer sv = new EchoServer(port);

        try {
            //sv.listen(); //Start listening for connections
        } catch (Exception ex) {
            System.out.println("ERROR - Could not listen for clients!");
        }

    }

    protected void clientConnected(ConnectionToClient client) {

        System.out.println("<Client Connected:" + client + ">");

    }

    synchronized protected void clientException(
            ConnectionToClient client, Throwable exception) {

        System.out.println("Client has disconnected");
    }

    //Method to check if its a command or not
    public void handleMessageFromServerString(String message) {
        Envelope env1 = null;

        if (message.charAt(0) == '#') {
            handleCommandFromServerString(message);
        } else {
            this.sendToAllClients("<ADMIN>" + message);
        }
    }
// if message is a command

    public void handleCommandFromServerString(String message) {
        if (message.equals("#stop")) {
            try {
                this.close();
            } catch (IOException ex) {
                Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        if (message.equals("#quit")) {
            try {
                this.close();
                System.exit(0);
            } catch (IOException ex) {
                Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        if (message.indexOf("#setPort") >= 0) {
            Integer port = Integer.parseInt(message.substring(8, message.length()).trim());

            EchoServer es = new EchoServer(port);
            this.port = port;
        }
        if (message.indexOf("#start") >= 0) {
            try {
                port = this.port;
                this.listen();
                System.out.println("Starting the server");
            } catch (IOException ex) {
                System.out.println("Couldnt start the server");;
            }
        }

        if (message.indexOf("#ison") >= 0) {
            String userName = message.substring(5, message.length()).trim();

            Thread[] clientThreadList = getClientConnections();

            for (int i = 0; i < clientThreadList.length; i++) {
                try {
                    ConnectionToClient recipient = ((ConnectionToClient) clientThreadList[i]);
                    String userid = recipient.getInfo("userid").toString();

                    if (userid.equals(userName)) {
                        System.out.println(userName + " is in room " + recipient.getInfo("room").toString());
                    }
                } catch (Exception ex) {
                    System.out.println("Unable to send a client");
                }
            }
            System.out.println(userName + " is not online");
        }

        if (message.indexOf("#userstatus") >= 0) {

            //ArrayList<String> userList = new ArrayList<String>();
            //String room = client.getInfo("room").toString();
            Thread[] clientThreadList = getClientConnections();

            for (int i = 0; i < clientThreadList.length; i++) {
                try {
                    ConnectionToClient recipient = ((ConnectionToClient) clientThreadList[i]);

                    System.out.println(recipient.getInfo("userid").toString() + " is in room " + recipient.getInfo("room").toString());

                } catch (Exception ex) {
                    System.out.println("Unable to send a client");
                }

            }

        }

        if (message.indexOf("#joinroom") >= 0) {

            String room1 = message.substring(9, message.length()).trim();
            String rooms[] = room1.split("\\s+");
            Thread[] clientThreadList = getClientConnections();

            for (int i = 0; i < clientThreadList.length; i++) {
                try {
                    ConnectionToClient recipient = ((ConnectionToClient) clientThreadList[i]);
                    if (recipient.getInfo("room").equals(rooms[0])) {
                        recipient.setInfo("room", rooms[1]);
                    }
                } catch (Exception ex) {
                    System.out.println("Unable to send a client");
                }
            }

        }
    }

    public void createFile() {
        try {
            File myObj = new File("filename.txt");
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
//to disable record button for all other users in room
    public void changeRecordToDisable(ConnectionToClient client) {
        String room = client.getInfo("room").toString();
        String clientId = client.getInfo("userid").toString();
        Thread[] clientThreadList = getClientConnections();

        for (int i = 0; i < clientThreadList.length; i++) {
            try {
                ConnectionToClient recipient = ((ConnectionToClient) clientThreadList[i]);
                if (recipient.getInfo("room").equals(room)) {
                    if (recipient.getInfo("userid").equals(clientId)) {
                        recipient.setInfo("disable", "No");
                        System.out.println("This is client's id");
                    } else {
                        Envelope env = new Envelope("disable", "", "");
                        recipient.sendToClient(env);
                        recipient.setInfo("disable", "Yes");
                        recipient.sendToClient("Your record button has been disabled");
                        System.out.println("User ID: " + recipient.getInfo("userid").toString() + " disable : " + recipient.getInfo("disable").toString());
                    }
                }
            } catch (Exception ex) {
                System.out.println("Unable to send a client");
            }
        }
    }

}
//End of EchoServer class
