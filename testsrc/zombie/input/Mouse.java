package zombie.input;

public final class Mouse {
    public static int wheelState;
    public static int wheelDelta;
    public static int absoluteX;
    public static int absoluteY;

    private Mouse() {
    }

    public static int getWheelState() {
        return wheelDelta;
    }

    public static int getXA() {
        return absoluteX;
    }

    public static int getYA() {
        return absoluteY;
    }
}
