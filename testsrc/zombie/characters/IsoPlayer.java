package zombie.characters;

import java.util.ArrayList;
import java.util.List;
import zombie.iso.IsoCell;
import zombie.iso.IsoMovingObject;
import zombie.network.fields.hit.HitInfo;

public final class IsoPlayer extends IsoMovingObject {
    private final IsoCell cell = new IsoCell();
    private final List<HitInfo> hitInfo = new ArrayList<>();

    public IsoPlayer(int id) {
        super(id);
    }

    public int getIndex() {
        return 0;
    }

    public List<HitInfo> getHitInfoList() {
        return hitInfo;
    }

    public IsoCell getCell() {
        return cell;
    }
}
