package cn.lokilanka.utils;

/**
 * action when receive message
 * use SAM to simplify input
 */
@FunctionalInterface
public interface ReceiveCallback {
    void onReceive(ClientSocket clientSocket, String msg);
}
