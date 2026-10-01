package maeiro.viewpointextendedsupport;

import viewpoint.Compat;
import viewpoint.FP;
import viewpoint.core.View;
import viewpoint.input.Look;
import viewpoint.input.ThirdPerson;
import zombie.input.GameKeyboard;
import zombie.input.Mouse;
import org.lwjgl.glfw.GLFW;
import zombie.iso.IsoCamera;

public final class BridgeTest {
    public static void main(String[] args) {
        Bridge.configure(70, 71, false, true, 56, true, 72, true);
        check(Bridge.shouldSkipSetupWizard(), "setup wizard is skipped by default");

        GameKeyboard.pressed.add(70);
        check(Bridge.interceptFirstPersonToggle(), "custom first-person binding must skip original");
        check(View.enabled, "custom first-person binding must enable Viewpoint");
        check(FP.resetCount == 1, "custom toggle must reset Viewpoint caches");
        check(Compat.checks == 1, "custom toggle must run compatibility check");
        check(Math.abs(Look.yaw - 1.25f) < 0.001f, "custom toggle must restore player yaw");

        GameKeyboard.pressed.add(71);
        check(Bridge.interceptThirdPersonPoll(), "custom third-person binding must skip original");
        check(ThirdPerson.active, "custom third-person binding must activate third person");

        GameKeyboard.pressed.add(72);
        check(Bridge.interceptThirdPersonPoll(), "view mode binding must skip original");
        check(!ThirdPerson.active, "view mode binding must switch to first person");
        ThirdPerson.active = true;

        GameKeyboard.down.add(56);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "hold binding must activate free cursor");
        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();
        check(!Bridge.isFreeCursor(), "free cursor must release after the hold key is released");

        Bridge.setAutoCursorRequested(true);
        check(Bridge.isFreeCursor(), "UI request must activate free cursor immediately");
        Bridge.setAutoCursorRequested(false);
        check(!Bridge.isFreeCursor(), "UI request release must restore captured cursor immediately");

        Bridge.noteLootCursor(true);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "Viewpoint loot cursor request must be preserved");

        GameKeyboard.down.add(56);
        Bridge.applyCursorOverride();

        check(Bridge.shouldSkipVanillaCursor(false), "free cursor mode must hide the vanilla world cursor");
        check(Bridge.shouldSkipViewpointCursor(false), "free cursor mode must suppress the Viewpoint world cursor");
        check(Bridge.shouldSkipCursorRender(), "free cursor mode must hide the world cursor renderer");
        check(Bridge.shouldSkipMouseCursorTexture(), "Viewpoint mode must suppress duplicate mouse cursor textures");
        check(Bridge.overrideMouseCursorVisibility(false), "free cursor mode must expose the hand cursor");
        check(Bridge.shouldSkipVanillaReticle(false), "free cursor must hide vanilla reticle");

        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();
        check(Bridge.shouldSkipVanillaCursor(false), "captured Viewpoint mode must hide the vanilla world cursor");
        check(Bridge.shouldSkipCursorRender(), "captured Viewpoint mode must hide the direct cursor renderer");
        check(Bridge.shouldSkipMouseCursorTexture(), "captured first-person mode must hide custom cursor texture");
        check(!Bridge.overrideMouseCursorVisibility(true), "captured Viewpoint mode must hide custom cursor texture");
        check(!Bridge.shouldSkipVanillaReticle(false), "captured mode must keep vanilla reticle");

        IsoCamera.Character player = (IsoCamera.Character) IsoCamera.character;
        player.vehicle = new Object();
        check(Bridge.isThirdPersonVehicle(), "third-person vehicle state must be detected");
        check(Bridge.shouldSkipCursorRender(), "third-person vehicle mode must hide the direct cursor renderer");
        check(Bridge.shouldSkipMouseCursorTexture(), "vehicle camera must hide custom cursor texture");
        check(!Bridge.overrideMouseCursorVisibility(true), "vehicle camera must hide mouse cursor icons");
        check(Bridge.shouldSkipVanillaReticle(false), "vehicle camera must hide vanilla reticle");
        GameKeyboard.down.add(56);
        Bridge.applyCursorOverride();
        check(Bridge.shouldSkipVanillaCursor(false), "third-person vehicle mode must hide the vanilla hand cursor");
        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();
        check(!Bridge.overrideMouseCursorUpdate(false), "vehicle camera must preserve camera cursor capture");
        player.vehicle = null;
        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();
        check(!Bridge.overrideMouseCursorUpdate(false), "leaving vehicle camera must allow normal update");

        Bridge.setAutoCursorRequested(true);
        Bridge.applyCursorOverride();
        check(Bridge.shouldSkipVanillaReticle(false), "free cursor mode must hide vanilla reticle");
        check(Bridge.shouldSkipCursorRender(), "automatic UI cursor must hide the world cursor renderer");
        check(Bridge.shouldSkipMouseCursorTexture(), "automatic UI cursor must suppress duplicate mouse cursor textures");
        check(Bridge.overrideMouseCursorVisibility(false), "automatic UI cursor must expose the hand cursor");
        GLFW.mode = 212993;
        check(!Bridge.overrideMouseCursorUpdate(false), "free cursor mode must let Viewpoint release mouse capture");
        check(GLFW.mode == 212993, "free cursor mode must preserve the native hand cursor mode");
        GLFW.mode = 212993;
        check(!Bridge.overrideLookMouseCursorUpdate(false), "free cursor mode must preserve Viewpoint's focus recovery");
        check(GLFW.mode == 212993, "focus recovery must keep the native hand cursor visible");

        Bridge.setAutoCursorRequested(false);
        Bridge.applyCursorOverride();
        check(!Bridge.overrideMouseCursorUpdate(false), "released cursor must allow normal update");
        check(GLFW.mode == 212993, "released cursor must restore normal system cursor");

        Bridge.configure(70, 71, false, false, 56, true, 72, true);
        Bridge.setAutoCursorRequested(true);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "UI option must enable the free cursor by default");
        Bridge.configure(70, 71, false, false, 56, false, 72, true);
        Bridge.applyCursorOverride();
        check(!Bridge.isFreeCursor(), "disabling automatic UI cursor must release the free cursor");
        Bridge.configure(70, 71, false, true, 56, true, 72, true);

        Mouse.wheelState = 1;
        Bridge.captureMouseWheel();
        Mouse.wheelState = 0;
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) < 5.0f,
                "mouse wheel up must move the third-person camera closer");
        Mouse.wheelState = -2;
        Bridge.captureMouseWheel();
        Mouse.wheelState = 0;
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) > 5.0f,
                "mouse wheel down must move the third-person camera farther away");
        Mouse.wheelState = 0;

        float zoomBeforeFreeCursor = Bridge.adjustThirdPersonBoom(5.0f);
        GameKeyboard.down.add(56);
        Bridge.applyCursorOverride();
        Mouse.wheelState = 1;
        Bridge.captureMouseWheel();
        Mouse.wheelState = 0;
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) == zoomBeforeFreeCursor,
                "free cursor must not consume third-person camera zoom");
        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();

        Bridge.configure(70, 71, false, true, 56, true, 72, true, true, false);
        check(!Bridge.shouldSkipSetupWizard(), "setup screen option can keep the wizard enabled");
        Bridge.configure(70, 71, false, true, 56, true, 72, true, true, true);
        check(Bridge.shouldSkipSetupWizard(), "setup screen option can skip the wizard");

        View.enabled = false;
        check(Bridge.enableViewpoint(), "startup option must enable Viewpoint");
        check(View.enabled, "startup option must leave Viewpoint enabled");
        int resetCount = FP.resetCount;
        check(Bridge.enableViewpoint(), "startup option must succeed when Viewpoint is already enabled");
        check(FP.resetCount == resetCount, "startup option must not toggle or reset an already-enabled view");
        View.enabled = false;
        check(Bridge.adjustThirdPersonBoom(5.0f) == 5.0f,
                "disabled Viewpoint mode must preserve the original camera distance");
        check(Bridge.overrideMouseCursorVisibility(true), "vanilla cursor visibility must be preserved outside Viewpoint");

        System.out.println("ViewpointExtendedSupport BridgeTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
