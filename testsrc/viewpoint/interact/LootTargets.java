package viewpoint.interact;

import java.util.ArrayList;

public final class LootTargets {
    static final int PERSON = 6;
    public static final ArrayList<zombie.iso.IsoObject> addedObjects = new ArrayList<>();

    private static int add(int kind, zombie.iso.IsoGridSquare square, zombie.iso.IsoObject object) {
        addedObjects.add(object);
        return addedObjects.size() - 1;
    }
}
