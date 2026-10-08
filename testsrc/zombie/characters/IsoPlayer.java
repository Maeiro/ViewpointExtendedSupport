package zombie.characters;

public class IsoPlayer extends zombie.iso.IsoMovingObject {
    public boolean CanSee(zombie.iso.IsoMovingObject target) {
        return target.visible;
    }
}
