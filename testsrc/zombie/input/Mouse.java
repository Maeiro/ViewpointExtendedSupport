package zombie.input;

public final class Mouse {
    public static int wheelState;
    public static int absoluteX;
    public static int absoluteY;

    private Mouse() {
    }

    public static int getWheelState() {
        return wheelState;
    }

    public static int getXA() {
        return absoluteX;
    }

    public static int getYA() {
        return absoluteY;
    }
}
