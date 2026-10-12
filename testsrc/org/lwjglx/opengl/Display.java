package org.lwjglx.opengl;

public final class Display {
    public static int width = 1920;
    public static int height = 1080;

    private Display() {
    }

    public static long getWindow() {
        return 1L;
    }

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }
}
