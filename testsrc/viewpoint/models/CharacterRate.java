package viewpoint.models;

import java.util.ArrayList;
import java.util.List;

public class CharacterRate {
    public enum Pose { LIVE, LAST }
    public static class Slot { public final List<Data> modelData = new ArrayList<>(); }
    public static class Data {
        public Model model = new Model();
        public Instance modelInstance = new Instance();
        public Object xfrm = new Object();
        public Object resolved = xfrm;
    }
    public static class Model { public boolean isStatic; public String Name = "TestAttachment"; }
    public static class Instance { public String parentBoneName = "Bip01_Prop1"; }
    public static boolean fail;

    static Object placed(Object character, Slot slot, int index, Pose pose) {
        if (fail) throw new NullPointerException("bone not available");
        return slot.modelData.get(index).resolved;
    }
}
