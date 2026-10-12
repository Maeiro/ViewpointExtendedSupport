package viewpoint.input;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.opengl.Display;

public final class Look {
    public static volatile float yaw;
    public static volatile float pitch;
    public static volatile boolean wantCapture = true;
    public static volatile boolean captured;

    public static boolean onUpdateMouseCursor() {
        long window = Display.getWindow();
        if (!wantCapture) {
            if (captured) {
                captured = false;
                GLFW.glfwSetInputMode(window, 208897, 212993);
                GLFW.glfwSetCursorPos(window, Display.getWidth() / 2.0, Display.getHeight() / 2.0);
            }
            return false;
        }
        captured = true;
        GLFW.glfwSetInputMode(window, 208897, 212995);
        return true;
    }
}
