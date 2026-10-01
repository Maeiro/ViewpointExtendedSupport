package zombie.characters;

import zombie.iso.IsoMovingObject;

public final class IsoZombie extends IsoMovingObject {
    public boolean dead;

    public IsoZombie(int id) {
        super(id);
    }

    public boolean isDead() {
        return dead;
    }
}
