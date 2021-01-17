package cn.lokilanka.utils;

import java.util.HashSet;

public class Session implements Runnable {
    private final ClientSocket user1;
    private final ClientSocket user2;
    private final ReceiveCallback receiveCallback;
    private static final String success = "Personal channel established!";
    private static final String close = "Personal channel closed.";
    private final HashSet<ClientSocket> personalSessions;

    Session(ClientSocket user1, ClientSocket User2, ReceiveCallback receiveCallback, HashSet<ClientSocket> personalSession) {
        this.user1 = user1;
        this.user2 = User2;
        this.receiveCallback = receiveCallback;
        this.personalSessions = personalSession;
        this.user1.setReceiveCallback(this.user2.personalCallback);
        this.user1.setPersonalChat(true);
        this.user2.setReceiveCallback(this.user1.personalCallback);
        this.user2.setPersonalChat(true);
    }

    @Override
    public void run() {
        user1.send(success, CommandParser.MessageType.Server);
        user2.send(success, CommandParser.MessageType.Server);
        while (user1.getPersonalChat() && user2.getPersonalChat()) {

        }
        user1.setReceiveCallback(receiveCallback);
        user2.setReceiveCallback(receiveCallback);
        user1.send(close, CommandParser.MessageType.Server);
        user2.send(close, CommandParser.MessageType.Server);
        user1.setPersonalChat(false);
        user2.setPersonalChat(false);
        synchronized (personalSessions) {
            personalSessions.remove(user1);
            personalSessions.remove(user2);
        }
    }

}
