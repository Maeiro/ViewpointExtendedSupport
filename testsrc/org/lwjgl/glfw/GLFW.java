package org.lwjgl.glfw;

import java.nio.DoubleBuffer;

public final class GLFW {
    public static int mode;
    public static double cursorX;
    public static double cursorY;
    public static int cursorPositionCalls;

    private GLFW() {
    }

    public static void glfwSetInputMode(long window, int modeKey, int value) {
        mode = value;
    }

    public static int glfwGetInputMode(long window, int modeKey) {
        return mode;
    }

    public static void glfwGetCursorPos(long window, DoubleBuffer x, DoubleBuffer y) {
        x.put(0, cursorX);
        y.put(0, cursorY);
    }

    public static void glfwSetCursorPos(long window, double x, double y) {
        cursorPositionCalls++;
        cursorX = x;
        cursorY = y;
    }
}
