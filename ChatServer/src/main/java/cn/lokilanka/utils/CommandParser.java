package cn.lokilanka.utils;

public class CommandParser {
    private static final String showUsers = "show";
    private static final String personalChat = "chat";
    private static final String login = "login";
    private static final String signup = "signup";

    public enum MessageType {
        Broadcast,
        Person,
        Server,
        Error,
        Command
    }

    private CommandParser() {

    }

    public static String validateCommand(String command) {
        String[] args = command.split(" ");

        if (args.length > 4)
            return "";
        return switch (args[0]) {
            case signup -> args.length == 2 && args[1].matches("([a-z]|[A-Z]){1,6}") ? signup : "";
            case login -> args.length == 2 && args[1].matches("([a-z]|[A-Z]){1,6}") ? login : "";
            case personalChat -> args.length == 2 && args[1].matches("([a-z]|[A-Z]){1,6}") ? personalChat : "";
            case showUsers -> args.length == 1 ? showUsers : "";
            default -> "";
        };
    }
}
