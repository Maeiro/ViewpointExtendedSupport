package viewpoint.game;

import viewpoint.core.Frame;
import viewpoint.input.FreeCam;

final class WorldToScreen {
    private float width;
    private float height;
    private FreeCam.Place place;

    WorldToScreen() {
    }

    boolean set(Frame frame, FreeCam.Place place, float width, float height) {
        this.place = place;
        this.width = width;
        this.height = height;
        return frame != null && width > 0 && height > 0;
    }

    boolean pixel(float x, float y, float z, float headHeight, float[] out) {
        out[0] = width / 2 + x + z + headHeight;
        out[1] = height / 2 + y + z + headHeight;
        return true;
    }

    FreeCam.Place place() {
        return place;
    }
}
