package viewpoint.interact;

public final class LootBoxes {
    public static float[] lastBox;

    private static void grow(int index, float minX, float minY, float minZ,
                             float maxX, float maxY, float maxZ) {
        lastBox = new float[]{index, minX, minY, minZ, maxX, maxY, maxZ};
    }
}
