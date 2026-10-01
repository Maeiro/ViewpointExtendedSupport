package viewpoint;

public final class FP {
    private static boolean cursorMode;
    public static int resetCount;

    public static void resetCaches() {
        resetCount++;
    }

    public static boolean cursor() {
        return cursorMode;
    }
}
