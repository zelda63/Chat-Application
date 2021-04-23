
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class overrides some of the methods defined in the abstract superclass
 * in order to give more functionality to the client.
 */
public class ChatClient extends AbstractClient {
    //Instance variables **********************************************

    /**
     * The interface type variable. It allows the implementation of the display
     * method in the client.
     */
    ChatIF clientUI;

    //Constructors ****************************************************
    /**
     * Constructs an instance of the chat client.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @param clientUI The interface type variable.
     */
    public ChatClient(String host, int port, ChatIF clientUI)
            throws IOException {
        super(host, port); //Call the superclass constructor
        this.clientUI = clientUI;
        //openConnection();
    }

    //Instance methods ************************************************
    /**
     * This method handles all data that comes in from the server.
     *
     * @param msg The message from the server.
     */
    public void handleMessageFromServer(Object msg) {
        if (msg instanceof Envelope) {
            handleCommandFromServer(msg);
        } else {
            clientUI.display(msg.toString());
        }
    }

    public void handleCommandFromServer(Object msg) {
        Envelope env = (Envelope) msg;
        if (env.getId().equals("who")) {
            ArrayList<String> userList = (ArrayList<String>) env.getContent();
          
        }
        if(env.getId().equals("transcript")){
            byte[] data = (byte[])env.getContent();
            String fileName = "C:\\Users\\Nimrat Sembhi\\Documents\\NetBeansProjects\\EchoServer\\Conversations\\" + env.getArg().toString() + ".txt";
            
            try {
                Files.write(Paths.get(fileName), data);
            } catch (IOException ex) {
                System.out.println("Error in handleCommandFromServer");            }
            
        }
        
        if(env.getId().equals("disable")){
            ((GUIConsole)clientUI).disableRecord();
            
        }
        
        if(env.getId().equals("enable")){
            ((GUIConsole)clientUI).enableRecord();
            
        }
    }

    /**
     * This method handles all data coming from the UI
     *
     * @param message The message from the UI.
     */
    public void handleMessageFromClientUI(String message) {

        if (message.charAt(0) == '#') {

            handleClientCommand(message);

        } else {
            try {
                sendToServer(message);
            } catch (IOException e) {
                clientUI.display("Could not send message to server.  Terminating client.......");
                quit();
            }
        }
    }

    /**
     * This method terminates the client.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {
        }
        System.exit(0);
    }

    public void connectionClosed() {

        System.out.println("Connection closed");

    }

    protected void connectionException(Exception exception) {

        System.out.println("Server has shut down - connection closed");

    }

    public void handleClientCommand(String message) {

        Envelope env = null;

        if (message.equals("#quit")) {
            clientUI.display("Shutting Down Client");
            quit();

        }

        if (message.equals("#logoff")) {
            clientUI.display("Disconnecting from server");
            try {
                closeConnection();
            } catch (IOException e) {
            };

        }

        if (message.indexOf("#setHost") >= 0) {

            if (isConnected()) {
                clientUI.display("Cannot change host while connected");
            } else {
                setHost(message.substring(8, message.length()).trim());
            }

        }

        if (message.indexOf("#setPort") >= 0) {

            if (isConnected()) {
                clientUI.display("Cannot change port while connected");
            } else {
                setPort(Integer.parseInt(message.substring(8, message.length()).trim()));
            }

        }

        if (message.indexOf("#login") >= 0) {

            if (isConnected()) {
                clientUI.display("already connected");
            } else {

                String userName = message.substring(6, message.length()).trim();
                env = new Envelope("login", "", userName);

                try {

                    openConnection();
                    clientUI.display("Logging in as " + userName);
                    this.sendToServer(env);
                } catch (IOException e) {
                    clientUI.display("failed to connect to server.");
                }
            }
        }

        if (message.indexOf("#join") >= 0) {

            String roomName = message.substring(5, message.length()).trim();
            env = new Envelope("join", "", roomName);
            try {
                //EchoServer es = new EchoServer(port);
                openConnection();
                clientUI.display("Joining: " + roomName);
                this.sendToServer(env);

            } catch (IOException e) {
                System.out.println("Unable to join room");
            }

        }

        if (message.indexOf("#pm") >= 0) {

            String messageWOCommand = message.substring(3, message.length()).trim();
            String recipient = messageWOCommand.substring(0, messageWOCommand.indexOf(" ")).trim();
            String pMessage
                    = messageWOCommand.substring(messageWOCommand.indexOf(" "), messageWOCommand.length()).trim();

            env = new Envelope("pm", recipient, pMessage);

        }

        if (message.indexOf("yell") >= 0) {

            String yellMessage = message.substring(5, message.length()).trim();

            env = new Envelope("yell", "", yellMessage);
            try {

                openConnection();

                this.sendToServer(env);

            } catch (IOException e) {
                clientUI.display("failed to yell");
            }

        }
        if (message.indexOf("record") >= 0) {

            env = new Envelope("record", "", "Recording has been started");
        
        }
        if (message.indexOf("stopRecord") >= 0) {

            env = new Envelope("stopRecord", "", "Recording has been stopped");
        
        }
        

        if (message.equals("#who")) {
            env = new Envelope("who", "", "");

        }
        if (env != null) {
            try {

                this.sendToServer(env);
            } catch (IOException e) {
                clientUI.display("failed to request user list.");
            }
        }

    }

}
//End of ChatClient class
