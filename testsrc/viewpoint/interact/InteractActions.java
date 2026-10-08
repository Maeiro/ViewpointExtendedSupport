package viewpoint.interact;

public final class InteractActions {
    private static Object aimed;
    private static boolean gathered = true;
    private static long aimedAt = Long.MAX_VALUE;

    private InteractActions() {
    }

    public static boolean isGathered() {
        return gathered;
    }

    public static long aimedAt() {
        return aimedAt;
    }

    public static void setAimed(Object target) {
        aimed = target;
    }

    static void run(zombie.characters.IsoPlayer player, int action) {
    }
}
