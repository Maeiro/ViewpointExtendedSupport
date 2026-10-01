package zombie.iso;

import zombie.core.textures.ColorInfo;

public class IsoMovingObject {
    private final int id;
    private boolean outlined;
    private int outlineColor;

    public IsoMovingObject(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }

    public boolean isOutlineHighlight(int playerIndex) {
        return outlined;
    }

    public void setOutlineHighlight(int playerIndex, boolean value) {
        outlined = value;
    }

    public int getOutlineHighlightCol(int playerIndex) {
        return outlineColor;
    }

    public void setOutlineHighlightCol(int playerIndex, float r, float g, float b, float a) {
        outlineColor = ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    public void setOutlineHighlightCol(int playerIndex, ColorInfo color) {
        outlineColor = color.packed;
    }
}
