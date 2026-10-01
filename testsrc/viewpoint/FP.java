package viewpoint;

import viewpoint.core.Frame;

public final class FP {
    private static boolean cursorMode;
    public static Frame[] frames;
    public static int resetCount;

    public static void resetCaches() {
        resetCount++;
    }

    public static boolean cursor() {
        return cursorMode;
    }
}
