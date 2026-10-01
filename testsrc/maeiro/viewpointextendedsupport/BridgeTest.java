package maeiro.viewpointextendedsupport;

import viewpoint.Compat;
import viewpoint.FP;
import viewpoint.core.View;
import viewpoint.core.Frame;
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
        check(Bridge.shouldSkipViewpointCrosshair(), "the Viewpoint center pixel must be replaced by the native reticle");

        check(Bridge.overrideAimingReticleX(0, 123) == -10000,
                "the vanilla reticle X must be hidden while the native reticle is active");
        check(Bridge.overrideAimingReticleY(0, 123) == -10000,
                "the vanilla reticle Y must be hidden while the native reticle is active");

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

        Vector3 cameraOrigin = new Vector3(0.0f, 0.0f, 1.6f);
        Vector3 cameraDirection = new Vector3(1.0f, 0.0f, -0.35f);
        cameraDirection.normalize();
        Vector3 endpoint = new Vector3(cameraOrigin.x + cameraDirection.x * 8.0f,
                cameraOrigin.y + cameraDirection.y * 8.0f,
                cameraOrigin.z + cameraDirection.z * 8.0f);
        Vector3 muzzlePosition = new Vector3(0.4f, 0.0f, 1.2f);
        Vector3 muzzleDirection = new Vector3(1.0f, 0.0f, 0.0f);
        Bridge.applyPz3dMuzzleCorrection(cameraOrigin, cameraDirection, endpoint,
                muzzlePosition, muzzleDirection, false);
        check(muzzleDirection.z < -0.3f,
                "first-person muzzle direction must follow the camera below the horizon");
        check(Math.abs(muzzlePosition.y) < 0.001f
                        && muzzlePosition.z < 1.6f
                        && muzzlePosition.x > 0.0f,
                "first-person muzzle origin must be centered on the camera ray");

        muzzlePosition.set(0.0f, 0.0f, 1.0f);
        muzzleDirection.set(1.0f, 0.0f, 0.0f);
        Bridge.applyPz3dMuzzleCorrection(cameraOrigin, cameraDirection, endpoint,
                muzzlePosition, muzzleDirection, true);
        check(muzzleDirection.z < -0.2f,
                "third-person muzzle direction must point from the muzzle to the camera target");

        IsoCamera.Character originalCamera = (IsoCamera.Character) IsoCamera.character;
        FakeCharacter character = new FakeCharacter();
        FakeBallisticsController controller = new FakeBallisticsController(character);
        IsoCamera.character = character;
        ThirdPerson.active = false;
        muzzlePosition.set(0.0f, 0.0f, 1.0f);
        muzzleDirection.set(1.0f, 0.0f, 0.0f);
        Look.pitch = -0.3f;
        Bridge.adjustViewpointMuzzle(controller, muzzlePosition, muzzleDirection);
        check(muzzleDirection.z < -0.2f,
                "ballistics hook must mutate the live muzzle direction");
        check(controller.isoAimingPosition.x == 4.0f
                        && controller.isoAimingPosition.z == 0.0f,
                "ballistics hook must use the native camera target as the 3D aim position");

        Frame frame = new Frame();
        frame.viewPitch = -0.5f;
        FP.frames = new Frame[]{frame};
        Look.pitch = 0.0f;
        muzzlePosition.set(0.0f, 0.0f, 1.0f);
        muzzleDirection.set(1.0f, 0.0f, 0.0f);
        Bridge.adjustViewpointMuzzle(controller, muzzlePosition, muzzleDirection);
        check(muzzleDirection.z < -0.2f,
                "ballistics hook must use the rendered frame pitch instead of the stale input pitch");
        FP.frames = null;
        IsoCamera.character = originalCamera;
        ThirdPerson.active = true;

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

    private static final class FakeBallisticsController {
        private final FakeCharacter isoGameCharacter;
        private final Vector3 muzzlePosition = new Vector3(0.0f, 0.0f, 1.0f);
        private final Vector3 isoAimingPosition = new Vector3();
        private final float[] cameraTargets = new float[5];

        private FakeBallisticsController(FakeCharacter character) {
            isoGameCharacter = character;
            cameraTargets[1] = 4.0f;
            cameraTargets[2] = 0.0f;
            cameraTargets[3] = 0.0f;
        }

        public int getID() {
            return 1;
        }

        public Vector3 getMuzzlePosition() {
            return muzzlePosition;
        }

        public Vector3 getIsoAimingPosition() {
            return isoAimingPosition;
        }

        public void getCameraTargets(float range, boolean includeCharacters) {
        }

        public int getNumberOfCameraTargets() {
            return 1;
        }

        public float[] getCameraTargets() {
            return cameraTargets;
        }
    }

    private static final class FakeCharacter {
        private final FakeWeapon weapon = new FakeWeapon();

        public FakeWeapon getAttackingWeapon() {
            return weapon;
        }
    }

    private static final class FakeWeapon {
        public boolean isRanged() {
            return true;
        }

        public float getMaxRange(FakeCharacter character) {
            return 8.0f;
        }
    }

}
