package maeiro.viewpointextendedsupport;

import me.zed_0xff.zombie_buddy.Patch;

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
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            if (Bridge.shouldSkipInteractionWheel()) {
                return true;
            }
            Bridge.captureMouseWheel();
            return false;
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

    @Patch(className = "viewpoint.SceneDrawer", methodName = "drawCrosshair", warmUp = true)
    public static class ViewpointCrosshair {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return Bridge.shouldSkipViewpointCrosshair();
        }
    }

    @Patch(className = "viewpoint.platform.Onboarding", methodName = "show", warmUp = true)
    public static class ViewpointSetupWizard {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return Bridge.shouldSkipSetupWizard();
        }
    }

    @Patch(className = "viewpoint.platform.Onboarding", methodName = "blocks", warmUp = true)
    public static class ViewpointSetupBlock {
        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.shouldSkipSetupBlock(result);
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
        @Patch.OnEnter
        public static void enter() {
            Bridge.prepareMouseCursorUpdate();
        }

        @Patch.OnExit
        public static void exit(@Patch.Return(readOnly = false) boolean result) {
            result = Bridge.overrideLookMouseCursorUpdate(result);
        }
    }

    @Patch(className = "viewpoint.interact.LootRows", methodName = "actions", warmUp = true)
    public static class ViewpointInteractionGroups {
        @Patch.OnExit
        public static void exit(@Patch.This(readOnly = true) Object rows) {
            Bridge.groupContextMenuActions(rows);
        }
    }

    @Patch(className = "viewpoint.interact.LootTargets", methodName = "people", warmUp = true)
    public static class ALifeInteractionTargets {
        @Patch.OnExit
        public static void exit(@Patch.Argument(0) Object player,
                                @Patch.Argument(1) Object square) {
            Bridge.addALifeInteractionTargets(player, square);
        }
    }

    @Patch(className = "viewpoint.interact.InteractActions", methodName = "run", warmUp = true)
    public static class GroupedInteractionAction {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter(@Patch.Argument(1) int action) {
            return Bridge.handleGroupedContextAction(action);
        }
    }

    @Patch(className = "viewpoint.interact.LootRows", methodName = "clear", warmUp = true)
    public static class ClearGroupedInteractionMenu {
        @Patch.OnEnter
        public static void enter(@Patch.This(readOnly = true) Object rows) {
            Bridge.clearGroupedContextMenu(rows);
        }
    }

    @Patch(className = "viewpoint.interact.LootRows", methodName = "build", warmUp = true)
    public static class RebuildGroupedInteractionMenu {
        @Patch.OnEnter
        public static void enter(@Patch.This(readOnly = true) Object rows) {
            Bridge.clearGroupedContextMenu(rows);
        }
    }

    @Patch(className = "viewpoint.interact.LootPanel", methodName = "besides", warmUp = true)
    public static class ViewpointInteractionIconSpacing {
        @Patch.OnExit
        public static void exit(@Patch.Argument(1) Object row,
                                @Patch.Argument(2) int iconSize,
                                @Patch.Return(readOnly = false) int spacing) {
            spacing = Bridge.adjustContextMenuIconSpacing(row, spacing, iconSize);
        }
    }

    @Patch(className = "viewpoint.interact.LootPanel", methodName = "draw", warmUp = true)
    public static class ViewpointInteractionMenuVisibility {
        @Patch.OnEnter(skipOn = true)
        public static boolean enter() {
            return Bridge.shouldSkipInteractionMenu();
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
