
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Nimrat Sembhi
 */
public class ServerConsole implements ChatIF {

    ChatClient server;

    EchoServer chat = null;

    /**
     * This method is responsible for the creation of the Client UI.
     *
     * @param args[0] The host to connect to.
     */
    public static void main(String[] args) {
        //String host = "";
        int port = 0;

        try {
            // host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            //host = "localhost";
            // port = DEFAULT_PORT;
        }
        ServerConsole sc = new ServerConsole(port);
        sc.accept();

    }
    //Constructors ****************************************************

    /**
     * Constructs an instance of the ClientConsole UI.
     *
     * @param host The host to connect to.
     * @param port The port to connect on.
     */
    public ServerConsole(int port) {
        port = port;
        //initiating echoServer
        chat = new EchoServer(port, this);
    }

    //Instance methods ************************************************
    /**
     * This method waits for input from the console. Once it is received, it
     * sends it to the client's message handler.
     */
    public void accept() {
        try {
            BufferedReader fromConsole
                    = new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (true) {
                message = fromConsole.readLine();
                chat.handleMessageFromServerString(message);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading console");
        }
    }

    /**
     * This method overrides the method in the ChatIF interface. It displays a
     * message onto the screen.
     *
     * @param message The string to be displayed.
     */
    public void display(String message) {
        System.out.println("> " + message);
    }

    public void displayUserList(ArrayList<String> userList, String room) {
        display("Users in " + room + ":");
        for (String user : userList) {
            display(user);
        }

    }
    //Class methods ***************************************************

}
