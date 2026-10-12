package maeiro.viewpointextendedsupport;

import viewpoint.FP;
import viewpoint.core.View;
import viewpoint.input.Look;
import viewpoint.interact.LootTargets;
import me.zed_0xff.zombie_buddy.Patch;
import zombie.input.GameKeyboard;
import zombie.input.Mouse;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoObject;

public final class InteractionTargetCyclingTest {
    private InteractionTargetCyclingTest() {
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        Patch hook = Patches.InteractionTargetCycling.class.getAnnotation(Patch.class);
        check(hook != null && "aimed".equals(hook.methodName()),
                "target cycling should be hooked into Viewpoint's aimed-target selection");
        check(Patches.InteractionTargetCycling.class.getDeclaredMethod("exit", LootTargets.Target.class) != null,
                "the advice return type must match Viewpoint's aimed-target return type");
        View.enabled = true;
        FP.cursorMode = false;
        IsoGridSquare square = new IsoGridSquare(5, 5, 0);
        Object first = LootTargets.addFoundTarget(1, square, new IsoObject());
        Object second = LootTargets.addFoundTarget(1, square, new IsoObject());
        Object third = LootTargets.addFoundTarget(1, square, new IsoObject());
        LootTargets.addFoundTarget(0, square, null);
        Bridge.configureInteractionCycling(203, 205);
        Look.yaw = 0.0f;
        Look.pitch = 0.0f;

        GameKeyboard.down.add(205);
        check(Bridge.cycleInteractionTarget(first) == second,
                "Right should select the next actionable nearby target and skip ground");
        GameKeyboard.down.clear();
        Bridge.configureInteractionCycling(203, 205);
        check(Bridge.cycleInteractionTarget(first) == second,
                "periodic option synchronization should preserve the selected target");
        check(Bridge.getInteractionCyclePosition() == 2 && Bridge.getInteractionCycleCount() == 3,
                "the interaction heading should expose the selected target position");

        GameKeyboard.down.add(205);
        check(Bridge.cycleInteractionTarget(first) == third,
                "repeated Right presses should cycle through nearby targets");
        GameKeyboard.down.clear();
        Look.yaw = 0.1f;
        check(Bridge.cycleInteractionTarget(first) == first,
                "moving the camera should restore Viewpoint's natural aimed target");
        check(Bridge.getInteractionCyclePosition() == 0 && Bridge.getInteractionCycleCount() == 0,
                "camera movement should clear the cycling indicator");

        Look.yaw = 0.0f;
        GameKeyboard.down.add(203);
        check(Bridge.cycleInteractionTarget(first) == third,
                "Left should wrap to the previous target");
        GameKeyboard.down.clear();

        GameKeyboard.down.add(56);
        Mouse.wheelDelta = 1;
        check(Bridge.shouldSkipInteractionWheel(), "Alt plus mouse wheel should be reserved for target cycling");
        check(Mouse.wheelDelta == 0, "reserved mouse wheel input should not scroll the interaction list");
        check(Bridge.cycleInteractionTarget(first) == first,
                "scrolling up with Alt should advance to the next nearby target");
        GameKeyboard.down.remove(56);
        Mouse.wheelDelta = 1;
        check(!Bridge.shouldSkipInteractionWheel(), "scrolling without the configured modifier should remain native");
        Mouse.wheelDelta = 0;

        Bridge.configureInteractionCycling(203, 205, 1);
        GameKeyboard.down.add(42);
        Mouse.wheelDelta = -1;
        check(Bridge.shouldSkipInteractionWheel(), "the scroll modifier should be configurable to Shift");
        check(Bridge.cycleInteractionTarget(first) == third,
                "scrolling down with Shift should cycle to the previous nearby target");
        GameKeyboard.down.remove(42);
        Bridge.configureInteractionCycling(203, 205, 4);

        Bridge.setInteractionMenuSuppressed(true);
        check(Bridge.cycleInteractionTarget(first) == first,
                "cycling should not replace the target while the interaction menu is suppressed");
        check(Bridge.getInteractionCycleCount() == 0,
                "suppressing the interaction menu should clear the cycle selection");
        Bridge.setInteractionMenuSuppressed(false);
        LootTargets.clearFound();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
