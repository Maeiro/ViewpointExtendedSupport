package viewpoint.input;

import viewpoint.core.Frame;

public final class ThirdPerson {
    public static volatile boolean active;

    public static boolean view(Frame frame, float yaw, float pitch, float[] eye) {
        if (!active) {
            return false;
        }
        eye[0] = 1.0f;
        eye[1] = 2.4494896f;
        eye[2] = 2.0f;
        return true;
    }
}
