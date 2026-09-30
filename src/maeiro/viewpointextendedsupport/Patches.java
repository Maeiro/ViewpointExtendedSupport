package maeiro.viewpointextendedsupport;

import me.zed_0xff.zombie_buddy.Patch;
import zombie.iso.Vector3;

public final class Patches {
    private Patches() {
    }

    @Patch(className = "viewpoint.FP", methodName = "toggle", warmUp = true)
    public static class FirstPersonToggle {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return Bridge.interceptFirstPersonToggle();
        }
    }

    @Patch(className = "viewpoint.input.ThirdPerson", methodName = "poll", warmUp = true)
    public static class ThirdPersonPoll {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return Bridge.interceptThirdPersonPoll();
        }
    }

    @Patch(className = "viewpoint.input.ThirdPerson", methodName = "boom", warmUp = true)
    public static class ThirdPersonBoom {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) float result) {
            result = Bridge.adjustThirdPersonBoom(result);
        }
    }

    @Patch(className = "viewpoint.interact.LootMenu", methodName = "wheel", warmUp = true)
    public static class LootWheel {
        @Patch.OnEnter
        public static void enter() {
            Bridge.captureMouseWheel();
        }
    }

    @Patch(className = "viewpoint.FP", methodName = "cursorMode", warmUp = true)
    public static class CursorMode {
        @Patch.OnExit
        public static void exit() {
            Bridge.applyCursorOverride();
        }
    }

    @Patch(className = "viewpoint.interact.LootMenu", methodName = "freeCursor", warmUp = true)
    public static class LootCursor {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = true) boolean result) {
            Bridge.noteLootCursor(result);
        }
    }

    @Patch(className = "viewpoint.Hooks", methodName = "skipIsoCursor")
    public static class IsoCursor {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.shouldSkipVanillaCursor(result);
        }
    }

    @Patch(className = "viewpoint.Hooks", methodName = "skipIsoReticle")
    public static class IsoReticle {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.shouldSkipVanillaReticle(result);
        }
    }

    @Patch(className = "viewpoint.Patch_IsoCursor", methodName = "enter", warmUp = true)
    public static class ViewpointIsoCursor {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.shouldSkipViewpointCursor(result);
        }
    }

    @Patch(className = "viewpoint.Patch_IsoReticle", methodName = "enter", warmUp = true)
    public static class ViewpointIsoReticle {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.shouldSkipViewpointReticle(result);
        }
    }

    @Patch(className = "zombie.core.physics.BallisticsController",
            methodName = "calculateMuzzlePosition", warmUp = true)
    public static class ViewpointMuzzleDirection {
        @Patch.OnExit
        public static void exit(@Patch.Argument(value = 1, readOnly = true) Vector3 direction) {
            Bridge.adjustViewpointMuzzleDirection(direction);
        }
    }

    @Patch(className = "zombie.characters.IsoPlayer", methodName = "setAngleFromAim", warmUp = true)
    public static class ViewpointAimPitch {
        @Patch.OnExit
        public static void exit(@Patch.This(readOnly = true) Object player) {
            Bridge.syncViewpointAimPitch(player);
        }
    }

    @Patch(className = "zombie.iso.sprite.IsoCursor", methodName = "render", warmUp = true)
    public static class DirectIsoCursor {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return Bridge.shouldSkipCursorRender();
        }
    }

    @Patch(className = "viewpoint.Hooks", methodName = "skipMouseCursorUpdate")
    public static class MouseCursorUpdate {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.overrideMouseCursorUpdate(result);
        }
    }

    @Patch(className = "viewpoint.input.Look", methodName = "onUpdateMouseCursor", warmUp = true)
    public static class LookMouseCursorUpdate {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.overrideLookMouseCursorUpdate(result);
        }
    }

    @Patch(className = "zombie.input.Mouse", methodName = "renderCursorTexture", warmUp = true)
    public static class DirectMouseCursorTexture {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return Bridge.shouldSkipMouseCursorTexture();
        }
    }

    @Patch(className = "zombie.input.Mouse", methodName = "isCursorVisible", warmUp = true)
    public static class DirectMouseCursorVisibility {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.overrideMouseCursorVisibility(result);
        }
    }

}
