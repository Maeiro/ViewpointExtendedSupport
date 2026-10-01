package zombie.network.fields.hit;

import zombie.iso.IsoMovingObject;

public final class HitInfo {
    private final IsoMovingObject object;

    public HitInfo(IsoMovingObject object) {
        this.object = object;
    }

    public IsoMovingObject getObject() {
        return object;
    }
}
