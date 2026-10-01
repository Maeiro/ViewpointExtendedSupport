package zombie.core.physics;

public final class Bullet {
    public static int positionUpdates;
    public static int rotationUpdates;
    public static float aimX;
    public static float aimHeight;
    public static float aimY;

    public static void updateBallisticsAimReticlePosition(int id, float x, float height, float y) {
        positionUpdates++;
        aimX = x;
        aimHeight = height;
        aimY = y;
    }

    public static void updateBallisticsAimReticleQuaternion(int id,
                                                              float x, float y, float z, float w) {
        rotationUpdates++;
    }
}
