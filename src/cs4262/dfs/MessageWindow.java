package cs4262.dfs;

public class MessageWindow {

    private final String[] window;
    private final int size;
    private int count = 0;

    public MessageWindow() {
        size = Main.hopCount / 2;
        window = new String[size];
    }

    public boolean shouldForwardMessage(String message) {
        message = message.substring(0, message.length() - (Main.hopCount+"").length()).trim();
        for (int i = 0; i < size; i++) {
            if (window[i] != null && window[i].equals(message)) {
                window[count] = message;
                count++;
                count %= size;
                return false;
            }
        }
        window[count] = message;
        count++;
        count %= size;
        return true;
    }

}
