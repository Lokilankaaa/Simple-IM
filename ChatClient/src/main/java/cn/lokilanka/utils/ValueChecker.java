package cn.lokilanka.utils;

public class ValueChecker {

    /**
     * @param str string to check
     * @return if str is a valid ip value
     */
    public static boolean isAddress(String str) {
        String pattern = "((([01]?\\d?\\d|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d?\\d|2[0-4]\\d|25[0-5]))|localhost";
        return str.matches(pattern);
    }

    /**
     * @param str string to check
     * @return if str is a valid integer value
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * use short circuiting
     *
     * @param str string to check
     * @return if str is a valid port value
     */
    public static boolean isPort(String str) {
        return isInteger(str) && Integer.parseInt(str) > 0 && Integer.parseInt(str) < 65536;
    }
}
