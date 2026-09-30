package zombie.iso.sprite;

import zombie.core.textures.ColorInfo;

public final class IsoReticle {
    private static final IsoReticle INSTANCE = new IsoReticle();
    public ColorInfo aimColor;

    public static IsoReticle getInstance(int playerIndex) {
        return playerIndex == 0 ? INSTANCE : null;
    }

    public void setAimColor(ColorInfo color) {
        aimColor = color;
    }
}
