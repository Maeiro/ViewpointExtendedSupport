package zombie.iso;

import java.util.ArrayList;

public final class IsoGridSquare {
    public int x;
    public int y;
    public int z;
    private final ArrayList<IsoMovingObject> movingObjects = new ArrayList<>();

    public IsoGridSquare(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public ArrayList<IsoMovingObject> getMovingObjects() {
        return movingObjects;
    }
}
