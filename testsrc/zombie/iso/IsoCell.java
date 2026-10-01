package zombie.iso;

import java.util.ArrayList;
import zombie.characters.IsoZombie;

public final class IsoCell {
    private final ArrayList<IsoZombie> zombies = new ArrayList<>();

    public ArrayList<IsoZombie> getZombieList() {
        return zombies;
    }
}
