package org.lwjgl.opengl;

public final class GL11 {
    public static final int GL_VIEWPORT = 2978;
    public static final int GL_SCISSOR_TEST = 3089;
    public static final int GL_COLOR_BUFFER_BIT = 16384;
    public static int clears;
    public static int viewportWidth = 1920;
    public static int viewportHeight = 1080;
    public static float red;
    public static float green;
    public static float blue;

    public static void glGetIntegerv(int name, int[] values) {
        values[0] = 0;
        values[1] = 0;
        values[2] = viewportWidth;
        values[3] = viewportHeight;
    }

    public static void glEnable(int name) {
    }

    public static void glScissor(int x, int y, int width, int height) {
    }

    public static void glClearColor(float r, float g, float b, float a) {
        red = r;
        green = g;
        blue = b;
    }

    public static void glClear(int mask) {
        clears++;
    }
}
