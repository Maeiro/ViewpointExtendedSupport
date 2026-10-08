package zombie.iso;

public class IsoMovingObject extends IsoObject {
    public float x;
    public float y;
    public float z;
    public boolean dead;
    public boolean visible = true;
    private final ModData modData = new ModData();

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public boolean isDead() {
        return dead;
    }

    public ModData getModData() {
        return modData;
    }

    public void setALifeUid(String uid) {
        modData.uid = uid;
    }

    public static final class ModData implements se.krka.kahlua.vm.KahluaTable {
        private String uid;

        @Override
        public Object rawget(Object key) {
            return "ProjectALifeUID".equals(key) ? uid : null;
        }
    }
}
