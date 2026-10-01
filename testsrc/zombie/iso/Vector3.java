package zombie.iso;

public class Vector3 {
    public float x;
    public float y;
    public float z;

    public Vector3() {
    }

    public Vector3(float x, float y, float z) {
        set(x, y, z);
    }

    public Vector3 set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3 normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        if (length > 0.0001f) {
            x /= length;
            y /= length;
            z /= length;
        }
        return this;
    }
}
