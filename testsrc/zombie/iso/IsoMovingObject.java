package zombie.iso;

public class IsoMovingObject extends IsoObject {
    public float x;
    public float y;
    public float z;
    public boolean dead;
    public boolean visible = true;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public boolean isDead() {
        return dead;
    }

}
