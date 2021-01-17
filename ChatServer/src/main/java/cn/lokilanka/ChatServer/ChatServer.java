package cn.lokilanka.ChatServer;

import cn.lokilanka.utils.UserManager;
import cn.lokilanka.utils.ValueChecker;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Scanner;

public class ChatServer implements Runnable {

    private volatile boolean running;
    private ServerSocket serverSocket;
    private final int port;
    private final UserManager userManager = new UserManager();

    /**
     * server service constructor
     *
     * @param port server socket port
     */
    ChatServer(int port) throws SQLException, ClassNotFoundException {
        this.port = port;
    }

    /**
     * start server service
     *
     * @see ChatServer#running
     * use volatile boolean running to avoid dead loop
     * listening for new socket join in
     */
    private void start() {
        running = true;
        try {
            serverSocket = new ServerSocket(this.port);
            while (running) {
                Socket socket = serverSocket.accept();
                userManager.addClient(socket);
            }
        } catch (SocketException e) {
            System.out.println("Server been closed");
            System.out.println(e.getMessage());
            this.stop();
        } catch (IOException e) {
            System.out.println("Server start error, caused by:");
            e.printStackTrace();
            this.stop();
        }
    }

    /**
     * stop server service
     *
     * @see ChatServer#running
     * set volatile boolean running to false to stop listening
     * stop all client socket and clear client list
     */
    public void stop() {
        try {
            running = false;
            userManager.stop();
            serverSocket.close();
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
        System.out.println("Server started");
        start();
    }

    static Scanner sc = new Scanner(System.in);

    /**
     * handle port value input
     *
     * @return int port value between 1 and 65535
     */
    private static int getPort() {
        String port;
        do {
            System.out.println("Input correct port name, range in (1,65535)");
            port = sc.nextLine();
        } while (!ValueChecker.isPort(port));
        return Integer.parseInt(port);
    }

    /**
     * waiting for manual server stop signal
     */
    private static void waitingForStop() {
        String enter;
        do {
            System.out.println("Enter 'stop' to stop server");
            enter = sc.nextLine();
        } while (!enter.equals("stop"));
    }


    public static void main(String[] args) {
        ChatServer server = null;
        try {
            server = new ChatServer(getPort());
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        Thread thread = new Thread(server);
        thread.start();
        waitingForStop();
        assert server != null;
        server.stop();
    }
}

