package cn.lokilanka.ChatClient;

import cn.lokilanka.utils.ClientSocket;
import cn.lokilanka.utils.CommandParser;
import cn.lokilanka.utils.ValueChecker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.net.Socket;

public class ChatClient extends JFrame {

    private ClientSocket client = null;
    private static boolean chatState = false;
    private JTextField tfText;
    private JTextArea taContent;
    private final JButton exitButton = new JButton("exit");
    private final JButton exitSessionButton = new JButton("exit session");
    private final JButton showButton = new JButton("show all");
    private JFrame jFrameWelcome;

    /**
     * 初始化窗体
     *
     * @param x
     * @param y
     * @param w
     * @param h
     */
    private void initFrame(int x, int y, int w, int h) {
        this.tfText = new JTextField();
        this.taContent = new JTextArea(30, 10);
        this.setBounds(x, y, w, h);
        this.setResizable(false);
        this.setLayout(new BorderLayout());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
                client.stop();
                System.exit(0);
            }
        });
        JPanel controlBar = new JPanel();
        controlBar.setLayout(new FlowLayout());
        controlBar.add(showButton);
        controlBar.add(exitSessionButton);
        controlBar.add(exitButton);
        controlBar.setSize(w - 20, 30);
        this.tfText.setSize(w - 20, 30);
        this.exitSessionButton.setVisible(false);
        this.taContent.setEditable(false);
        JScrollPane jScrollPane = new JScrollPane(this.taContent, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane.setSize(w - 20, h - 100);
        this.add(controlBar, BorderLayout.NORTH);
        this.add(jScrollPane, BorderLayout.CENTER);
        this.add(tfText, BorderLayout.SOUTH);
        this.taContent.setText(showMenu());
        this.exitButton.addActionListener((e) -> {
            client.stop();
            System.exit(0);
        });
        this.showButton.addActionListener((e) -> {
            client.send("$$show", CommandParser.MessageType.Command);
        });
        this.exitSessionButton.addActionListener((e) -> {
            client.send("$$quit", CommandParser.MessageType.Command);
            chatState = false;
            this.exitSessionButton.setVisible(false);
        });
        this.tfText.addActionListener((actionEvent) -> {
            String str = tfText.getText().trim();
            tfText.setText("");
            if (str.equals(""))
                return;
            if (!str.equals("$quit") && client.getStatus()) {
                if (!chatState && str.startsWith("$$chat")) {
                    client.send(str, CommandParser.MessageType.Command);
                } else if (chatState) {
                    client.send(str, CommandParser.MessageType.Person);
                } else
                    client.send(str, CommandParser.MessageType.Broadcast);
            }
            if (str.equals("$quit")) {
                client.stop();
                System.exit(0);
            }
        });
        this.setVisible(false);
    }

    public static void setChatState(boolean chatState) {
        ChatClient.chatState = chatState;
    }

    /**
     * handle build server socket
     * input host and check if is ip or localhost
     * input port and check if between 1 and 65535
     *
     * @return server socket
     */
    private Socket getSocket() {
        String host;
        do {
            host = JOptionPane.showInputDialog("Input correct host name, must be valid ip or localhost");
        } while (!ValueChecker.isAddress(host));
        String port;
        do {
            port = JOptionPane.showInputDialog("Input correct port name, range in (1,65535)");
        } while (!ValueChecker.isPort(port));
        try {
            return new Socket(host, Integer.parseInt("5355"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Server socket start error, please re-enter host and port");
            return getSocket();
        }
    }

    public static String showMenu() {
        return "Please enter following command or just type using broadcast : \n"
                + "-----------------------------------------------------------\n"
                + "$$chat [target name]         :To chat personally with target.\n";
    }

    private void welcome() {
        JButton loginButton = new JButton("login");
        JButton signupButton = new JButton("sign up");
        JLabel jLabel = new JLabel("Please login or sign up first!", JLabel.CENTER);
        JTextField username = new JTextField();
        username.setToolTipText("[username must be in 1-6 english characters!]");
        jFrameWelcome = new JFrame("Welcome to simple chat!");
        jFrameWelcome.setSize(400, 200);
        jFrameWelcome.setLayout(new GridLayout(4, 1));
        jFrameWelcome.setLocation(300, 300);
        jFrameWelcome.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        jFrameWelcome.add(jLabel);
        jFrameWelcome.add(username);
        jFrameWelcome.add(controlPanel);
        loginButton.addActionListener((e) -> {
            String str = username.getText().trim();
            if (str.matches("([a-z]|[A-Z]){1,6}")) {
                client.send("$$login " + str, CommandParser.MessageType.Command);
            }
        });
        signupButton.addActionListener((e) -> {
            String str = username.getText().trim();
            if (str.matches("([a-z]|[A-Z]){1,6}")) {
                client.send("$$signup " + str, CommandParser.MessageType.Command);
            }
        });
        controlPanel.add(loginButton);
        controlPanel.add(signupButton);
        jFrameWelcome.setVisible(true);
    }

    /**
     * create new client service thread and start
     */
    private void startClient() {
        try {
            client = new ClientSocket(getSocket(), (sourceClient, msg) -> {
                if (msg.contains("successfully!")) {
                    jFrameWelcome.dispose();
                    this.setVisible(true);
                }
                if (msg.contains("not registered")) {
                    JOptionPane.showMessageDialog(jFrameWelcome, "User not registered!");
                    return;
                }
                if (msg.contains("Personal channel established!")) {
                    chatState = true;
                    this.exitSessionButton.setVisible(true);
                }
                if (msg.contains("closed")) {
                    chatState = false;
                    this.exitSessionButton.setVisible(false);
                }
                this.taContent.append(msg + "\n");
                this.taContent.setCaretPosition(this.taContent.getDocument().getLength());
            },
                    (errorClient, msg) -> {
                        this.taContent.append("Server connection error, caused by:\n");
                        this.taContent.append(msg);
                        this.taContent.setCaretPosition(this.taContent.getDocument().getLength());
                        errorClient.stop();
                    });
            welcome();
            initFrame(200, 200, 800, 550);
            Thread thread = new Thread(client);
            thread.start();
            thread.join();
            if (client.getStatus()) {
                client.stop();
            }
        } catch (Exception e) {
            System.out.println("Client init failed, caused by:");
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.startClient();
    }
}
