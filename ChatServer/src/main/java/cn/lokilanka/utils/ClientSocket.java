package cn.lokilanka.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSocket implements Runnable {

    public ReceiveCallback personalCallback = (clientSocket, msg) -> {
        String[] msgs = msg.split("%%%");
        if (msgs[0].equals(CommandParser.MessageType.Person.name())) {
            clientSocket.send(clientSocket.getName() + " says:" + msgs[1], CommandParser.MessageType.Person);
            send(clientSocket.getName() + " says:" + msgs[1], CommandParser.MessageType.Person);
        } else if (msgs[0].equals(CommandParser.MessageType.Broadcast.name())) {
            send(msgs[1], CommandParser.MessageType.Broadcast);
        } else if (msgs[1].equals("$$quit")) {
            System.out.println(clientSocket.getName() + " has quit.");
        } else
            clientSocket.send(msg, CommandParser.MessageType.Error);
    };

    private volatile boolean running;
    private volatile boolean personalChat = false;
    private String id;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private ReceiveCallback receiveCallback;
    private final ErrorCallback errorCallback;
    private String name = null;

    public void setPersonalChat(boolean personalChat) {
        this.personalChat = personalChat;
    }

    public boolean getPersonalChat() {
        return personalChat;
    }


    public void setReceiveCallback(ReceiveCallback receiveCallback) {
        this.receiveCallback = receiveCallback;
    }

    /**
     * getter for client socket's Id
     * Id is initialized in constructor
     *
     * @return client socket's Id
     */
    public String getSocketId() {
        return id;
    }

    public boolean getStatus() {
        return running;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * client socket default constructor
     *
     * @param socket          socket for connection between server and client
     * @param receiveCallback handle action when receiving message
     * @param errorCallback   handle action when exception happening
     * @throws IOException happened when getInputStream/getOutputStream
     */
    public ClientSocket(Socket socket, ReceiveCallback receiveCallback, ErrorCallback errorCallback) throws IOException {
        this.socket = socket;
        this.receiveCallback = receiveCallback;
        this.errorCallback = errorCallback;
        this.inputStream = new DataInputStream(socket.getInputStream());
        this.outputStream = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * client socket constructor with client id
     *
     * @param socket          socket for connection between server and client
     * @param receiveCallback handle action when receiving message
     * @param errorCallback   handle action when exception happening
     * @param id              client's id
     * @throws IOException happened when getInputStream/getOutputStream
     */
    public ClientSocket(Socket socket, ReceiveCallback receiveCallback, ErrorCallback errorCallback, String id) throws IOException {
        this(socket, receiveCallback, errorCallback);
        this.id = id;
    }

    /**
     * send message to client
     *
     * @param msg content to send
     */

    public void send(String msg, CommandParser.MessageType messageType) {
        try {
            msg = messageType.toString() + "%%%" + msg;
            outputStream.writeUTF(msg);
            outputStream.flush();
        } catch (IOException e) {
            if (running) {
                errorCallback.onError(this, e.getMessage());
            }
        }
    }

    /**
     * start client listening, handle receiving and exception
     *
     * @see ClientSocket#running
     * use volatile boolean running to avoid dead loop
     */

    private void start()

    {
        String accept;
        running = true;
        try {
            while (running) {
                accept = inputStream.readUTF();
                if (personalChat && accept.split("%%%")[1].equals("$$quit")) {
                    receiveCallback.onReceive(this, accept);
                    setPersonalChat(false);
                    continue;
                }
                receiveCallback.onReceive(this, accept);
            }
        } catch (IOException e) {
            setPersonalChat(false);
            if (running) {
                errorCallback.onError(this, e.getMessage());
            }
        }
    }

    /**
     * stop client listening
     *
     * @see ClientSocket#running
     * set volatile boolean running to false and close socket
     */

    public void stop() {
        try {
            running = false;
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * implement runnable override run()
     * start listening client
     */
    @Override
    public void run() {
        start();
    }
}


