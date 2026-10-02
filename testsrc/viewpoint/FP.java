package viewpoint;

public final class FP {
    private static boolean cursorMode;
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
