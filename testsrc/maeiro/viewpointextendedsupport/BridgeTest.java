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
import zombie.iso.Vector3;

public final class BridgeTest {
    public static void main(String[] args) {
        Bridge.configure(70, 71, false, true, 56, true, 72, true);

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
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "UI request must activate free cursor");
        Bridge.setAutoCursorRequested(false);
        Bridge.applyCursorOverride();
        check(!Bridge.isFreeCursor(), "UI request release must restore captured cursor");

        Bridge.noteLootCursor(true);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "Viewpoint loot cursor request must be preserved");

        GameKeyboard.down.add(56);
        Bridge.applyCursorOverride();

        check(Bridge.shouldSkipVanillaCursor(false), "Viewpoint cursor must stay hidden");
        check(Bridge.shouldSkipCursorRender(), "direct cursor render must stay hidden");
        check(Bridge.shouldSkipMouseCursorTexture(), "free cursor must hide the duplicate custom cursor texture");
        check(!Bridge.overrideMouseCursorVisibility(true), "Viewpoint must hide its duplicate cursor texture");
        check(Bridge.shouldSkipVanillaReticle(false), "free cursor must hide vanilla reticle");

        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();
        check(Bridge.shouldSkipMouseCursorTexture(), "captured first-person mode must hide custom cursor texture");
        check(!Bridge.overrideMouseCursorVisibility(true), "captured Viewpoint mode must hide custom cursor texture");
        check(!Bridge.shouldSkipVanillaReticle(false), "captured mode must keep vanilla reticle");
        check(!Bridge.shouldSkipViewpointReticle(false), "captured Viewpoint reticle must remain available");

        zombie.core.Core core = zombie.core.Core.getInstance();
        check(core.showReticleTexture, "Viewpoint must preserve the vanilla reticle texture");
        check(core.showValidTargetReticleTexture, "Viewpoint must preserve the valid-target ring");
        check(core.crosshairTextureIndex == 2, "Viewpoint must preserve the vanilla crosshair pieces");

        Look.pitch = 0.0f;
        Vector3 direction = new Vector3(1.0f, 0.0f, 0.25f);
        Bridge.adjustViewpointMuzzleDirection(direction);
        check(Math.abs(direction.x - 1.0f) < 0.001f && Math.abs(direction.z) < 0.001f,
                "level Viewpoint aim must use a level muzzle direction");

        Look.pitch = -0.5f;
        direction.set(1.0f, 0.0f, 0.0f);
        Bridge.adjustViewpointMuzzleDirection(direction);
        check(direction.x > 0.8f && direction.z < -0.4f,
                "looking down must pitch the muzzle down");

        IsoCamera.Character player = (IsoCamera.Character) IsoCamera.character;
        player.vehicle = new Object();
        check(Bridge.isThirdPersonVehicle(), "third-person vehicle state must be detected");
        check(Bridge.shouldSkipMouseCursorTexture(), "vehicle camera must hide custom cursor texture");
        check(Bridge.shouldSkipVanillaReticle(false), "vehicle camera must hide vanilla reticle");
        check(!Bridge.overrideMouseCursorUpdate(false), "vehicle camera must preserve camera cursor capture");
        player.vehicle = null;
        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();
        check(!Bridge.overrideMouseCursorUpdate(false), "leaving vehicle camera must allow normal update");

        Bridge.setAutoCursorRequested(true);
        Bridge.applyCursorOverride();
        check(Bridge.shouldSkipVanillaReticle(false), "free cursor mode must hide vanilla reticle");
        check(Bridge.overrideMouseCursorUpdate(false), "free cursor mode must hide system cursor");
        check(GLFW.mode == 212994, "free cursor mode must use a hidden system cursor");

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

        Bridge.setViewEnabled(false);
        check(core.showReticleTexture, "reticle texture option must be restored outside Viewpoint");
        check(core.showValidTargetReticleTexture, "valid-target option must be restored outside Viewpoint");
        check(core.crosshairTextureIndex == 2, "crosshair option must be restored outside Viewpoint");
        check(Bridge.adjustThirdPersonBoom(5.0f) == 5.0f,
                "disabled Viewpoint mode must preserve the original camera distance");
        check(Bridge.overrideMouseCursorVisibility(true), "vanilla cursor visibility must be preserved outside Viewpoint");

        System.out.println("ViewpointExtendedSupport BridgeTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

}
