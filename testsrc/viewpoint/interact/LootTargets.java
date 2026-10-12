package viewpoint.interact;

import java.util.ArrayList;

public final class LootTargets {
    static final int GROUND = 0;
    static final int PERSON = 6;
    private static final ArrayList<Target> found = new ArrayList<>();
    public static final ArrayList<zombie.iso.IsoObject> addedObjects = new ArrayList<>();

    public static void clearFound() {
        found.clear();
    }

    public static Target addFoundTarget(int kind, zombie.iso.IsoGridSquare square,
                                         zombie.iso.IsoObject object) {
        Target target = new Target();
        target.kind = kind;
        target.square = square;
        target.object = object;
        found.add(target);
        return target;
    }

    private static int add(int kind, zombie.iso.IsoGridSquare square, zombie.iso.IsoObject object) {
        addedObjects.add(object);
        return addedObjects.size() - 1;
    }

    public static final class Target {
        int kind;
        zombie.iso.IsoGridSquare square;
        zombie.iso.IsoObject object;

        private boolean same(Target other) {
            return other != null && kind == other.kind && square == other.square && object == other.object;
        }

        private void set(Target other) {
            kind = other.kind;
            square = other.square;
            object = other.object;
        }
    }
}
