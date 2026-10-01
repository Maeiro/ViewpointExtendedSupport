package zombie.core.textures;

public final class ColorInfo {
    public final String name;
    public final float r;
    public final float g;
    public final float b;
    public final float a;
    public int packed;

    public ColorInfo(String name) {
        this.name = name;
        this.r = 0.0f;
        this.g = 0.0f;
        this.b = 0.0f;
        this.a = 1.0f;
    }

    public ColorInfo(float r, float g, float b, float a) {
        this.name = null;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public ColorInfo setABGR(int color) {
        packed = color;
        return this;
    }
}
