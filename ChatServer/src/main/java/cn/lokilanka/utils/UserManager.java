package cn.lokilanka.utils;

import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.*;


public class UserManager {
    private final ArrayList<ClientSocket> clientList = new ArrayList<>();
    private final HashSet<ClientSocket> personalSession = new HashSet<>();
    private final HashMap<String, ClientSocket> onlineUsers = new HashMap<>();
    private final DatabaseManager databaseManager = new DatabaseManager("user_table.db");

    public UserManager() throws SQLException, ClassNotFoundException {

    }

    private void sessionChat(ClientSocket clientSocket, String target) {
        ClientSocket targetSocket = onlineUsers.get(target);
        if (targetSocket != null) {
            if (clientSocket.getName().equals(targetSocket.getName())) {
                clientSocket.send("Cannot chat personally with yourself!", CommandParser.MessageType.Error);
                return;
            }
            if (personalSession.contains(clientSocket) || personalSession.contains(targetSocket)) {
                clientSocket.send("This user is busy now. Please wait for a moment!", CommandParser.MessageType.Error);
                return;
            }
            synchronized (personalSession) {
                personalSession.add(clientSocket);
                personalSession.add(targetSocket);
            }
            Session session = new Session(clientSocket, targetSocket, this::receiveMsg, personalSession);
            new Thread(session).start();
        } else {
            clientSocket.send("The user is not online.", CommandParser.MessageType.Error);
        }
    }

    private void showUsers(ClientSocket clientSocket) {
        if (clientSocket.getName() != null) {
            StringBuilder userList = new StringBuilder().append("Current online users: ");
            synchronized (onlineUsers) {
                onlineUsers.forEach((k, v) -> userList.append("\n").append(k));
            }
            clientSocket.send(userList.toString(), CommandParser.MessageType.Server);
        }
    }

    private void login(ClientSocket clientSocket, String name) {
        try {
            if (databaseManager.selectUser(name)) {
                clientSocket.setName(name);
                synchronized (onlineUsers) {
                    onlineUsers.put(name, clientSocket);
                }
                clientSocket.send("Login successfully!", CommandParser.MessageType.Server);
                broadCastWithExcept(Collections.singletonList(clientSocket), clientSocket.getName() + " join in");
            } else
                clientSocket.send("User not registered!", CommandParser.MessageType.Error);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    private void signup(ClientSocket clientSocket, String name) {
        try {
            if (databaseManager.createUser(name)) {
                clientSocket.send("User registered successfully!", CommandParser.MessageType.Server);
                clientSocket.setName(name);
                synchronized (onlineUsers) {
                    onlineUsers.put(name, clientSocket);
                }
                broadCastWithExcept(Collections.singletonList(clientSocket), clientSocket.getName() + " join in");
            } else
                clientSocket.send("User name has been registered. Please try another one!", CommandParser.MessageType.Error);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            clientSocket.send("Unknown error happened when registering.", CommandParser.MessageType.Error);
        }
    }

    public void receiveMsg(ClientSocket clientSocket, String msg) {
        if (msg.startsWith("Command")) {
            msg = msg.split("%%%")[1];
            switch (CommandParser.validateCommand(msg.substring(2))) {
                case "signup" -> signup(clientSocket, msg.substring(9));
                case "login" -> login(clientSocket, msg.substring(8));
                case "chat" -> sessionChat(clientSocket, msg.substring(7));
                case "show" -> showUsers(clientSocket);
                default -> clientSocket.send("Invalid Command!", CommandParser.MessageType.Server);
            }
        } else {
            msg = msg.split("%%%")[1];
            broadCast(clientSocket, msg);
        }
    }

    /**
     * add new connect client socket to list
     * define action when receive message and exception happen
     *
     * @param socket new join in client socket
     */
    public void addClient(Socket socket) {
        try {
            final ClientSocket clientSocket = new ClientSocket(
                    socket,
                    this::receiveMsg,
                    (clientSocket2, msg) -> {
                        clientSocket2.stop();
                        synchronized (onlineUsers) {
                            onlineUsers.remove(clientSocket2.getName());
                        }
                        synchronized (clientList) {
                            clientList.remove(clientSocket2);
                        }
                        System.out.println("client:" + clientSocket2.getSocketId() + " removed, caused by:");
                        System.out.println(msg);
                        broadCast(clientSocket2.getName() + " quit chat");
                    },
                    socket.getInetAddress().toString().substring(1) + ":" + socket.getPort());
            synchronized (clientList) {
                clientList.add(clientSocket);
            }
            Thread t = new Thread(clientSocket);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        synchronized (clientList) {
            for (ClientSocket client : clientList) {
                client.stop();
            }
        }
        clientList.clear();
    }

    /**
     * broadcast message to all connected client except some in list
     *
     * @param exceptList the client socket list contain not send client
     * @param msg        the message content
     */
    public void broadCastWithExcept(List<ClientSocket> exceptList, String msg) {
        System.out.println(msg);
        synchronized (clientList) {
            for (ClientSocket socket : clientList) {
                if (!exceptList.contains(socket)) {
                    socket.send(msg, CommandParser.MessageType.Broadcast);
                }
            }
        }
    }

    /**
     * broadcast message to all connected client
     *
     * @param msg the message content
     */
    public void broadCast(String msg) {
        System.out.println(msg);
        synchronized (onlineUsers) {
            for (ClientSocket socket : onlineUsers.values()) {
                socket.send(msg, CommandParser.MessageType.Broadcast);
            }
        }
    }

    /**
     * broadcast message to all connected client
     *
     * @param sourceSocket the client socket which send the message
     * @param msg          the message content
     */
    public void broadCast(ClientSocket sourceSocket, String msg) {
        String content = sourceSocket.getName() + " says:" + msg;
        broadCast(content);
    }
}
