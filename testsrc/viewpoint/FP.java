package viewpoint;

import viewpoint.core.Frame;

public final class FP {
    private static boolean cursorMode;
    private static final Frame[] frames = { new Frame() };
    public static int resetCount;

    public static void resetCaches() {
        resetCount++;
    }

    public static void toggleCursorMode() {
        cursorMode = !cursorMode;
    }

    public static boolean cursor() {
        return cursorMode;
    }
}
