/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.IOException;

import java.util.ArrayList;

/**
 *
 * @author Nimrat Sembhi
 */
public class GUIConsole extends JFrame implements ChatIF {

    final public static int DEFAULT_PORT = 5555;
    ChatClient client;

    private JButton closeB = new JButton("Logoff");
    private JButton openB = new JButton("Login");
    private JButton sendB = new JButton("Send");
    private JButton quitB = new JButton("Quit");
    private JButton whoB = new JButton("User List");
    private JButton pmB = new JButton("PM");
    public JButton recordB = new JButton("Record");

    private static boolean recordBClicked = false;
    public JComboBox whoCB = new JComboBox();

    private JTextField portTxF = new JTextField("5555");
    private JTextField hostTxF = new JTextField("127.0.0.1");
    private JTextField messageTxF = new JTextField("");
    private JTextField userTxF = new JTextField("");

    private JLabel portLB = new JLabel("Port: ", JLabel.RIGHT);
    private JLabel hostLB = new JLabel("Host: ", JLabel.RIGHT);
    private JLabel messageLB = new JLabel("Message: ", JLabel.RIGHT);
    private JLabel userLB = new JLabel("User Id: ", JLabel.RIGHT);

    private JTextArea messageList = new JTextArea();

    public GUIConsole(ChatClient client) {
        this.client = client;
    }

    public GUIConsole(String host, int port, String userId) {

        super("Simple Chat GUI");
        setSize(300, 400);
        setLayout(new BorderLayout(5, 5));
        JPanel bottom = new JPanel();
        add("Center", messageList);
        add("South", bottom);
        bottom.setLayout(new GridLayout(8, 2, 5, 5));
        bottom.add(hostLB);
        bottom.add(hostTxF);
        bottom.add(portLB);
        bottom.add(portTxF);

        bottom.add(userLB);
        bottom.add(userTxF);

        bottom.add(messageLB);
        bottom.add(messageTxF);

        bottom.add(whoB);
        bottom.add(whoCB);

        bottom.add(pmB);
        bottom.add(sendB);
        bottom.add(openB);
        bottom.add(closeB);
        bottom.add(recordB);
        bottom.add(quitB);

        portTxF.setText(port + "");
        hostTxF.setText(host);
        userTxF.setText(userId);

        setVisible(true);

        openB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send("#setHost " + hostTxF.getText());
                send("#setPort " + portTxF.getText());
                send("#login " + userTxF.getText());
                // display(messageTxF.getText() + "\n");
            }
        }
        );

        sendB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send(messageTxF.getText());
                // display(messageTxF.getText() + "\n");
            }
        }
        );

        closeB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display("Logging off \n");
                send("#logoff");
            }
        }
        );

        recordB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
                if(recordB.getText() == "Record"){
                display("Starting recording \n");
                recordB.setText("Stop Recording");
                send("#record");
                }
                else{
                display("Stopping recording \n");
                recordB.setText("Record");
                send("#stopRecord");
                }
               
            }
        }
        );

       
         
        quitB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send("#quit");
            }
        }
        );

        whoB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send("#who");
            }
        }
        );

        pmB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send("#pm " + whoCB.getSelectedItem() + " " + messageTxF.getText());
            }
        }
        );

        try {
            client = new ChatClient(host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection!!!!"
                    + " Terminating client.");
            System.exit(1);
        }

    }
    
    public void disableRecord(){
    recordB.setEnabled(false);
    }
    public void enableRecord(){
    recordB.setEnabled(true);
    }

    public void display(String message) {
        messageList.insert(message + "\n", 0);

    }

    public void displayUserList(ArrayList<String> userList, String room) {
        for (String user : userList) {
            whoCB.addItem(user);
        }
    }

    public void send(Object message) {
        client.handleMessageFromClientUI(message.toString());
    }

    public static void main(String[] args) {
        String host = "";
        int port = 0;  //The port number
        String userId = "";
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        } catch (ArrayIndexOutOfBoundsException e) {
            host = "localhost";
            port = DEFAULT_PORT;
        }

        try {
            userId = args[2];
        } catch (ArrayIndexOutOfBoundsException e) {
            userId = "guest";
        }
        GUIConsole console = new GUIConsole(host, port, userId);
    }

}
