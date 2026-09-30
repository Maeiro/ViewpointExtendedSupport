package zombie.core;

import zombie.core.textures.ColorInfo;

public final class Core {
    private static final Core INSTANCE = new Core();

    public boolean showReticleTexture = true;
    public boolean showValidTargetReticleTexture = true;
    public int crosshairTextureIndex = 2;
    public final ColorInfo targetColor = new ColorInfo("target");
    public final ColorInfo noTargetColor = new ColorInfo("no-target");

    public static Core getInstance() {
        return INSTANCE;
    }

    public boolean getOptionShowReticleTexture() {
        return showReticleTexture;
    }

    public void setOptionShowReticleTexture(boolean value) {
        showReticleTexture = value;
    }

    public boolean getOptionShowValidTargetReticleTexture() {
        return showValidTargetReticleTexture;
    }

    public void setOptionShowValidTargetReticleTexture(boolean value) {
        showValidTargetReticleTexture = value;
    }

    public int getOptionCrosshairTextureIndex() {
        return crosshairTextureIndex;
    }

    public void setOptionCrosshairTextureIndex(int value) {
        crosshairTextureIndex = value;
    }

    public ColorInfo getTargetColor() {
        return targetColor;
    }

    public ColorInfo getNoTargetColor() {
        return noTargetColor;
    }
}
