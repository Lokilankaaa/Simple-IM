package cn.lokilanka.utils;

/**
 * action when exception happen
 * use SAM to simplify input
 */
@FunctionalInterface
public interface ErrorCallback {
    void onError(ClientSocket clientSocket, String msg);
}
