package cs4262.dfs;

public class MessageWindow {

    private final int[] window;
    private final int size;
    private int count = 0;
    private static MessageWindow instance;
    private long lastTime;

    private MessageWindow() {
        size = Main.hopCount / 2;
        window = new int[size];
    }

    public static MessageWindow getInstance() {
        if (MessageWindow.instance == null) {
            MessageWindow.instance = new MessageWindow();
        }
        return MessageWindow.instance;
    }

    public boolean shouldCheckOrForwardMessage(int queryId) {
        //It has been some time since we send the last message
        boolean result = true;
        if ((System.currentTimeMillis() - lastTime) < 500) {
            for (int i = 0; i < size; i++) {
                if (window[i] == queryId) {
                    result = false;
                    break;
                }
            }
        }
        lastTime = System.currentTimeMillis();
        window[count] = queryId;
        count++;
        count %= size;
        return result;
    }
}
