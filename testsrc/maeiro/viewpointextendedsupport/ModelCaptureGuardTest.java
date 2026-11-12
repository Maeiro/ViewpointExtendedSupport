package maeiro.viewpointextendedsupport;

import viewpoint.models.CharacterRate;
import viewpoint.models.CharacterRate.Data;
import viewpoint.models.CharacterRate.Slot;

public class ModelCaptureGuardTest {
    public static void main(String[] args) {
        Slot slot = new Slot();
        Data data = new Data();
        slot.modelData.add(data);
        Object[] call = { null, slot, new Object(), false, false, CharacterRate.Pose.LIVE, 0f, 0f, 0f, 0f };
        check(!ModelCaptureGuard.shouldSkip(call), "animated model is not affected");
        data.model.isStatic = true;
        check(!ModelCaptureGuard.shouldSkip(call), "complete static model is not affected");
        data.xfrm = null;
        check(!ModelCaptureGuard.shouldSkip(call), "valid cached placement works without raw xfrm");
        data.xfrm = new Object();
        data.resolved = null;
        check(ModelCaptureGuard.shouldSkip(call), "null cached placement is caught even with valid raw xfrm");
        for (int i = 0; i < 100; i++) check(ModelCaptureGuard.shouldSkip(call), "missing transform stays deferred");
        data.resolved = new Object();
        check(!ModelCaptureGuard.shouldSkip(call), "capture recovers once the transform is ready");
        CharacterRate.fail = true;
        check(ModelCaptureGuard.shouldSkip(call), "unready bone lookup is deferred");
        CharacterRate.fail = false;
        check(!ModelCaptureGuard.shouldSkip(call), "bone failure does not permanently disable capture");
        check(!ModelCaptureGuard.shouldSkip(new Object[0]), "unknown signature is left alone");
        System.out.println("ModelCaptureGuardTest passed");
    }

    private static void check(boolean result, String description) {
        if (!result) throw new AssertionError(description);
    }
}
