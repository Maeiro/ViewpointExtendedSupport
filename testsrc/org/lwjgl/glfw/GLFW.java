package org.lwjgl.glfw;

public final class GLFW {
    public static int mode;

    private GLFW() {
    }

    public static void glfwSetInputMode(long window, int modeKey, int value) {
        mode = value;
    }
}
