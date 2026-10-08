package maeiro.viewpointextendedsupport;

import viewpoint.Compat;
import viewpoint.FP;
import viewpoint.core.View;
import viewpoint.input.Look;
import viewpoint.input.ThirdPerson;
import viewpoint.interact.InteractActions;
import zombie.input.GameKeyboard;
import zombie.input.Mouse;
import org.lwjgl.glfw.GLFW;
import zombie.iso.IsoCamera;
import viewpoint.interact.LootRows;

public final class BridgeTest {
    public static void main(String[] args) {
        Bridge.configure(70, 71, false, true, 56, true, 72, true);
        check(!Bridge.shouldSkipInteractionMenu(), "Viewpoint interaction menu must remain visible when suppression is inactive");
        Bridge.setInteractionMenuSuppressed(true);
        check(Bridge.shouldSkipInteractionMenu(), "inventory suppression must hide the Viewpoint interaction menu");
        Bridge.setInteractionMenuSuppressed(false);
        check(!Bridge.shouldSkipInteractionMenu(), "closing inventory must restore the Viewpoint interaction menu");
        check(Bridge.requestInteractionOptions(), "deferred interaction request should reset the action collector");
        check(!InteractActions.isGathered(), "deferred interaction request should permit gathering the focused target again");
        check(InteractActions.aimedAt() < System.nanoTime() - 100_000_000L,
                "deferred interaction request should bypass the normal aim grace period");
        check(Bridge.shouldSkipSetupWizard(), "setup wizard is skipped by default");
        Object contextMenuIcon = new Object();
        Object vanillaWashIcon = new Object();
        Object vanillaClothingIcon = new Object();
        Bridge.clearContextMenuIcons();
        Bridge.clearVanillaContextMenuIcons();
        Bridge.setContextMenuIcon("Drink", contextMenuIcon);
        Bridge.setContextMenuIcon("Wash", contextMenuIcon);
        Bridge.setVanillaContextMenuIcon("Wash", vanillaWashIcon);
        Bridge.setVanillaContextMenuIcon("Wash: All Clothing", vanillaClothingIcon);

        LootRows groupedActions = new LootRows();
        groupedActions.addHeading("White Toilet");
        groupedActions.addAction("Drink", 0);
        groupedActions.addAction("Wash: Yourself", 1);
        groupedActions.addAction("Wash: All Clothing", 2);
        groupedActions.addAction("Rest", 3);
        Bridge.groupContextMenuActions(groupedActions);
        check(groupedActions.rows().size() == 4, "grouped actions must show category entries at the root");
        check("Wash  >".equals(groupedActions.rows().get(2).name()), "nested actions must be represented by an activatable category");
        check(groupedActions.rows().get(1).icon() == contextMenuIcon, "mapped action should use its CMI icon");
        check(groupedActions.rows().get(2).icon() == vanillaWashIcon, "vanilla submenu icon should take precedence over its CMI fallback");
        check(Bridge.adjustContextMenuIconSpacing(groupedActions.rows().get(2), 0, 20) == 28,
                "icon spacing must reserve room for the category icon");
        int washGroup = groupedActions.rows().get(2).action();
        check(Bridge.handleGroupedContextAction(washGroup), "selecting a category must open its child menu");
        check(groupedActions.rows().size() == 4, "submenu must contain Back and the category actions");
        check("< Back".equals(groupedActions.rows().get(1).name()), "submenu must provide a way back to its parent");
        check("Yourself".equals(groupedActions.rows().get(2).name()), "submenu action label must omit its repeated category");
        check(groupedActions.rows().get(2).action() == 1, "submenu action must preserve the original action index");
        check(groupedActions.rows().get(2).icon() == vanillaWashIcon, "submenu actions should inherit the parent vanilla icon");
        check(groupedActions.rows().get(3).icon() == vanillaClothingIcon,
                "vanilla leaf icons must be preserved for individual submenu actions");
        check(!Bridge.handleGroupedContextAction(groupedActions.rows().get(2).action()),
                "leaf actions must continue through Viewpoint's original action handler");
        check(Bridge.handleGroupedContextAction(groupedActions.rows().get(1).action()), "Back must return to the parent menu");
        check("Wash  >".equals(groupedActions.rows().get(2).name()), "Back must restore the parent category list");

        Bridge.clearVanillaContextMenuIcons();
        Bridge.refreshContextMenuIcons();
        check(groupedActions.rows().get(2).icon() == contextMenuIcon,
                "clearing captured vanilla icons should restore the CMI fallback");

        Bridge.configure(70, 71, false, true, 56, true, 72, true,
                true, true, 12, 13, false);
        check(groupedActions.rows().size() == 5
                        && "Wash: Yourself".equals(groupedActions.rows().get(2).name()),
                "disabling cascading menus must restore Viewpoint's original action rows");
        check(groupedActions.rows().get(2).icon() == contextMenuIcon,
                "disabling cascading menus must retain CMI icons on the flat action list");
        LootRows ungroupedActions = new LootRows();
        ungroupedActions.addAction("Wash: Yourself", 0);
        Bridge.groupContextMenuActions(ungroupedActions);
        check(ungroupedActions.rows().size() == 1
                        && "Wash: Yourself".equals(ungroupedActions.rows().get(0).name()),
                "disabling grouping must keep Viewpoint's original action list");
        Bridge.configure(70, 71, false, true, 56, true, 72, true);
        Bridge.clearContextMenuIcons();
        Bridge.refreshContextMenuIcons();
        check(ungroupedActions.rows().get(0).icon() == null,
                "changing the selected icon pack must remove stale menu icons");

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

        Bridge.configure(70, 71, false, true, 1003, true, 44, true,
                true, true, 12, 13, true, packModifiers(0, 1, 0, 0));
        ThirdPerson.active = false;
        GameKeyboard.pressed.add(44);
        Bridge.interceptThirdPersonPoll();
        check(!ThirdPerson.active, "composite view binding must not trigger without its modifier");
        GameKeyboard.down.add(42);
        GameKeyboard.pressed.add(44);
        Bridge.interceptThirdPersonPoll();
        check(ThirdPerson.active, "Shift+Z view binding must toggle third person");
        check(!GameKeyboard.pressed.contains(44), "composite view binding must consume its key press");
        GameKeyboard.down.remove(42);

        GameKeyboard.down.add(1003);
        Bridge.configure(70, 71, false, true, 1003, true, 44, true,
                true, true, 12, 13, true, packModifiers(0, 1, 0, 0));
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "mouse button 4 must activate the default hold binding");
        Bridge.configure(70, 71, false, true, 1003, true, 44, true,
                true, true, 12, 13, true, packModifiers(1, 1, 0, 0));
        Bridge.applyCursorOverride();
        check(!Bridge.isFreeCursor(), "modified mouse hold binding must wait for its modifier");
        GameKeyboard.down.add(42);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "mouse button 4 with Shift must activate the configured hold binding");
        GameKeyboard.down.remove(42);
        GameKeyboard.down.remove(1003);
        Bridge.applyCursorOverride();
        check(!Bridge.isFreeCursor(), "modified mouse hold binding must release when its chord is broken");
        Bridge.configure(70, 71, false, true, 56, true, 72, true);

        GameKeyboard.down.add(56);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "hold binding must activate free cursor");
        check(!FP.cursor(), "holding free cursor must not overwrite Viewpoint's native toggle state");
        Look.wantCapture = true;
        Bridge.prepareMouseCursorUpdate();
        check(!Look.wantCapture, "held free cursor must release Viewpoint mouse capture");
        FP.toggleCursorMode();
        Bridge.applyCursorOverride();
        check(FP.cursor(), "Viewpoint's middle-button toggle must remain usable while the hold key is down");
        GameKeyboard.down.remove(56);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "native middle-button cursor state must survive releasing the hold key");
        FP.toggleCursorMode();
        Bridge.applyCursorOverride();
        check(!Bridge.isFreeCursor(), "native middle-button toggle must close the cursor after hold release");

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
        GLFW.mode = 212995;
        check(Bridge.shouldSkipVanillaCursor(false), "captured Viewpoint mode must hide the vanilla world cursor");
        check(Bridge.shouldSkipCursorRender(), "captured Viewpoint mode must hide the direct cursor renderer");
        check(Bridge.shouldSkipMouseCursorTexture(), "captured first-person mode must hide custom cursor texture");
        check(!Bridge.overrideMouseCursorVisibility(true), "captured Viewpoint mode must hide custom cursor texture");
        check(!Bridge.shouldSkipVanillaReticle(false), "captured mode must keep vanilla reticle");
        GLFW.mode = 212993;
        check(Bridge.overrideMouseCursorVisibility(true), "main menu must preserve the native cursor while Viewpoint is enabled");
        GLFW.mode = 212995;

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

        Bridge.configure(70, 71, false, false, 56, true, 72, true);
        Bridge.setAutoCursorRequested(true);
        Bridge.applyCursorOverride();
        check(Bridge.isFreeCursor(), "UI option must enable the free cursor by default");
        Bridge.setAutoCursorRequested(false);
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

        Bridge.setInventoryOpen(true);
        Bridge.setInteractionMenuSuppressed(true);
        Mouse.wheelState = 2;
        float zoomBeforeInventoryScroll = Bridge.adjustThirdPersonBoom(5.0f);
        check(Patches.LootWheel.enter(), "the wheel hook must skip Viewpoint's hidden menu handler");
        Bridge.pollThirdPersonZoom();
        check(Bridge.shouldSkipInteractionWheel(), "hidden interaction menu must stop handling inventory scroll");
        check(Mouse.wheelState == 2, "inventory scrolling must not be consumed by the Viewpoint menu");
        check(Bridge.adjustThirdPersonBoom(5.0f) == zoomBeforeInventoryScroll,
                "inventory scrolling must not zoom the third-person camera");
        Bridge.setInteractionMenuSuppressed(false);
        check(Bridge.shouldSkipInteractionWheel(),
                "an open inventory must reserve the mouse wheel even when menu hiding is disabled");
        check(Patches.LootWheel.enter(), "inventory mode must bypass Viewpoint wheel handling");
        Bridge.setInventoryOpen(false);
        check(!Bridge.shouldSkipInteractionWheel(), "closing inventory must restore Viewpoint wheel handling");
        Mouse.wheelState = 0;

        float zoomBeforeKeys = Bridge.adjustThirdPersonBoom(5.0f);
        GameKeyboard.pressed.add(12);
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) > zoomBeforeKeys,
                "minus default binding must zoom the third-person camera out");
        GameKeyboard.pressed.add(13);
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) == zoomBeforeKeys,
                "equals default binding must zoom the third-person camera in");

        Bridge.configure(70, 71, false, true, 56, true, 72, true,
                false, true, 80, 81);
        float zoomBeforeCustomKey = Bridge.adjustThirdPersonBoom(5.0f);
        GameKeyboard.pressed.add(80);
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) > zoomBeforeCustomKey,
                "configured zoom binding must work even when mouse-wheel zoom is disabled");
        GameKeyboard.pressed.add(81);
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) == zoomBeforeCustomKey,
                "configured zoom-in binding must reverse zoom-out");

        Bridge.configure(70, 71, false, true, 56, true, 72, true,
                false, true, 80, 81, true, packModifiers(0, 0, 6, 0));
        float zoomBeforeCompositeKey = Bridge.adjustThirdPersonBoom(5.0f);
        GameKeyboard.pressed.add(80);
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) == zoomBeforeCompositeKey,
                "composite zoom binding must not trigger without its modifiers");
        GameKeyboard.down.add(29);
        GameKeyboard.down.add(56);
        GameKeyboard.pressed.add(80);
        Bridge.pollThirdPersonZoom();
        check(Bridge.adjustThirdPersonBoom(5.0f) > zoomBeforeCompositeKey,
                "Ctrl+Alt zoom binding must trigger with both modifiers");
        GameKeyboard.down.remove(29);
        GameKeyboard.down.remove(56);

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

    private static int packModifiers(int cursor, int viewMode, int zoomOut, int zoomIn) {
        return cursor | viewMode << 3 | zoomOut << 6 | zoomIn << 9;
    }
}
