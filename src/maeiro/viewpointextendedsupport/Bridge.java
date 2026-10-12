package maeiro.viewpointextendedsupport;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import me.zed_0xff.zombie_buddy.Exposer;

public final class Bridge {
    private static final int LEFT_SHIFT_KEY = 42;
    private static final int RIGHT_SHIFT_KEY = 54;
    private static final int LEFT_CONTROL_KEY = 29;
    private static final int RIGHT_CONTROL_KEY = 157;
    private static final int LEFT_ALT_KEY = 56;
    private static final int RIGHT_ALT_KEY = 184;
    private static final int DEFAULT_PREVIOUS_INTERACTION_KEY = 203;
    private static final int DEFAULT_NEXT_INTERACTION_KEY = 205;
    private static final int MODIFIER_SHIFT = 1;
    private static final int MODIFIER_CONTROL = 2;
    private static final int MODIFIER_ALT = 4;
    private static final int GROUP_ACTION_START = 1_000_000_000;
    private static final int GROUP_BACK_ACTION = Integer.MAX_VALUE;
    private static final Map<String, Object> interactionIcons = new HashMap<>();
    private static final Map<String, Object> vanillaInteractionIcons = new HashMap<>();
    private static final ArrayList<Float> companionDogTagPosition = new ArrayList<>(2);
    private static final float[] companionDogTagPixel = new float[2];
    private static final int GLFW_CURSOR = 208897;
    private static final int GLFW_CURSOR_NORMAL = 212993;
    private static final int GLFW_CURSOR_HIDDEN = 212994;
    private static final int GLFW_CURSOR_DISABLED = 212995;
    private static final DoubleBuffer cursorXBuffer = ByteBuffer.allocateDirect(Double.BYTES)
            .order(ByteOrder.nativeOrder()).asDoubleBuffer();
    private static final DoubleBuffer cursorYBuffer = ByteBuffer.allocateDirect(Double.BYTES)
            .order(ByteOrder.nativeOrder()).asDoubleBuffer();

    private static volatile boolean holdFreeCursor;
    private static volatile int freeCursorKey;
    private static volatile int freeCursorModifiers;
    private static volatile boolean autoCursorInUi = true;
    private static volatile boolean debugLogging;
    private static volatile boolean groupContextMenuActionsEnabled = true;
    private static volatile boolean skipSetupWizard;
    private static volatile boolean autoCursorRequested;
    private static volatile boolean inventoryOpen;
    private static volatile boolean interactionMenuSuppressed;
    private static volatile boolean forcedCursor;
    private static volatile boolean freeCursorOverrideActive;
    private static boolean cursorCaptureModeOverridden;
    private static volatile boolean lootCursorRequested;
    private static volatile boolean systemCursorHidden;
    private static volatile long systemCursorWindow;
    private static volatile boolean uiCursorCenterPending;
    private static volatile boolean uiMousePositionSyncFailureLogged;
    private static volatile String lastCursorLifecycleState;

    private static volatile Field viewEnabled;
    private static volatile Field thirdPersonActive;
    private static volatile Field cursorMode;
    private static volatile Field lookWantCapture;
    private static volatile Field lootRows;
    private static volatile Field lootRowName;
    private static volatile Field lootRowAction;
    private static volatile Field lootRowIcon;
    private static volatile Field lootRowEnabled;
    private static volatile Field lootRowsSelected;
    private static volatile Constructor<?> lootRowConstructor;
    private static volatile Field interactionActionsGathered;
    private static volatile Field interactionActionsAimedAt;
    private static volatile InteractionMenu activeInteractionMenu;
    private static volatile Field lookYaw;
    private static volatile Field lookPitch;
    private static volatile Method keyDown;
    private static volatile Method resetCaches;
    private static volatile Method getCameraCharacter;
    private static volatile Method getPlayerVehicle;
    private static volatile Class<?> vehicleMethodClass;
    private static volatile Method getDisplayWindow;
    private static volatile Method getDisplayWidth;
    private static volatile Method getDisplayHeight;
    private static volatile Method glfwGetCursorPos;
    private static volatile Method glfwSetCursorPos;
    private static volatile Method getCoreInstance;
    private static volatile Method coreGetScreenHeight;
    private static volatile Field gameMouseX;
    private static volatile Field gameMouseY;
    private static volatile Method glfwSetInputMode;
    private static volatile Method glfwGetInputMode;
    private static volatile Method gameMouseGetX;
    private static volatile Method gameMouseGetY;
    private static volatile Field viewpointFrames;
    private static volatile Field freeCameraActive;
    private static volatile Field freeCameraPlace;
    private static volatile Constructor<?> worldToScreenConstructor;
    private static volatile Method worldToScreenSet;
    private static volatile Method worldToScreenPixel;
    private static volatile Method screenWidth;
    private static volatile Method screenHeight;
    private static volatile Object worldToScreen;
    private static volatile ALifeTargetAccess alifeTargetAccess;
    private static volatile InteractionCycleAccess interactionCycleAccess;
    private static volatile Object selectedInteractionTarget;
    private static volatile int selectedInteractionPosition;
    private static volatile int selectedInteractionCount;
    private static volatile int previousInteractionKey = DEFAULT_PREVIOUS_INTERACTION_KEY;
    private static volatile int nextInteractionKey = DEFAULT_NEXT_INTERACTION_KEY;
    private static volatile boolean previousInteractionKeyDown;
    private static volatile boolean nextInteractionKeyDown;
    private static volatile float interactionCycleYaw;
    private static volatile float interactionCyclePitch;
    private static volatile boolean alifeAimTargetFailureLogged;

    private static volatile long vanillaCursorHookCalls;
    private static volatile long viewpointCursorHookCalls;
    private static volatile long directCursorRenderCalls;
    private static volatile long vanillaReticleHookCalls;
    private static volatile long viewpointReticleHookCalls;
    private static volatile long mouseCursorHookCalls;
    private static volatile long lookMouseCursorHookCalls;
    private static volatile long mouseCursorTextureHookCalls;
    private static volatile long mouseCursorVisibilityHookCalls;
    private static volatile long mouseCursorVisibleCalls;
    private static volatile long mouseCursorHiddenCalls;
    private static volatile long directCursorRenderSkippedCalls;
    private static volatile long diagnosticTicks;
    private static volatile boolean diagnosticStateInitialized;
    private static volatile boolean diagnosticViewEnabled;
    private static volatile boolean diagnosticFreeCursor;
    private static volatile boolean diagnosticAutoCursor;
    private static volatile boolean diagnosticThirdPerson;
    private static volatile boolean diagnosticVehicle;
    private static volatile boolean contextGroupingWarningLogged;
    private static volatile boolean alifeTargetAccessWarningLogged;
    private static volatile boolean alifeTargetHookLogged;
    private static volatile boolean alifeTargetAddedLogged;
    private static volatile boolean alifeTargetFailureLogged;
    private static volatile boolean interactionCycleFailureLogged;
    private static volatile boolean interactionCycleHookLogged;
    private static volatile boolean interactionCycleKeyboardLogged;

    static {
        Exposer.exposeClass(Bridge.class, "ViewpointExtendedSupport");
    }

    private Bridge() {
    }

    public static void configure(boolean holdCursor, int cursorKey, boolean autoUi,
                                 boolean debug, boolean skipWizard, boolean groupActions,
                                 int cursorModifiers) {
        holdFreeCursor = holdCursor;
        freeCursorKey = Math.max(0, cursorKey);
        freeCursorModifiers = cursorModifiers
                & (MODIFIER_SHIFT | MODIFIER_CONTROL | MODIFIER_ALT);
        autoCursorInUi = autoUi;
        debugLogging = debug;
        skipSetupWizard = skipWizard;
        if (groupContextMenuActionsEnabled != groupActions) {
            groupContextMenuActionsEnabled = groupActions;
            InteractionMenu menu = activeInteractionMenu;
            if (menu != null) {
                if (groupActions) {
                    groupContextMenuActions(menu.rowsObject);
                } else {
                    restoreInteractionMenu(menu);
                    menu.grouped = false;
                    if (applyFlatInteractionIcons(menu)) {
                        activeInteractionMenu = menu;
                    } else {
                        activeInteractionMenu = null;
                    }
                }
            }
        }
    }

    public static synchronized void clearContextMenuIcons() {
        interactionIcons.clear();
    }

    public static synchronized void clearVanillaContextMenuIcons() {
        vanillaInteractionIcons.clear();
    }

    public static boolean requestInteractionOptions() {
        try {
            Field gathered = interactionActionsGathered;
            if (gathered == null) {
                gathered = field("viewpoint.interact.InteractActions", "gathered");
                interactionActionsGathered = gathered;
            }
            Field aimedAt = interactionActionsAimedAt;
            if (aimedAt == null) {
                aimedAt = field("viewpoint.interact.InteractActions", "aimedAt");
                interactionActionsAimedAt = aimedAt;
            }
            if (gathered == null || aimedAt == null) {
                return false;
            }
            gathered.setBoolean(null, false);
            aimedAt.setLong(null, System.nanoTime() - 150_000_000L);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void configureInteractionCycling(int previousKey, int nextKey) {
        previousKey = Math.max(0, previousKey);
        nextKey = Math.max(0, nextKey);
        if (previousInteractionKey == previousKey && nextInteractionKey == nextKey) return;
        previousInteractionKey = previousKey;
        nextInteractionKey = nextKey;
        previousInteractionKeyDown = false;
        nextInteractionKeyDown = false;
        clearInteractionTargetSelection();
        if (debugLogging) {
            System.out.println("[Viewpoint Extended Support] interaction cycling configured: previousKey="
                    + previousKey + ", nextKey=" + nextKey);
        }
    }

    public static int getInteractionCyclePosition() {
        return selectedInteractionPosition;
    }

    public static int getInteractionCycleCount() {
        return selectedInteractionCount;
    }

    public static Object cycleInteractionTarget(Object aimedTarget) {
        boolean previousDown = isDown(previousInteractionKey);
        boolean nextDown = isDown(nextInteractionKey);
        boolean previousPressed = previousDown && !previousInteractionKeyDown;
        boolean nextPressed = nextDown && !nextInteractionKeyDown;
        previousInteractionKeyDown = previousDown;
        nextInteractionKeyDown = nextDown;

        if (debugLogging && !interactionCycleHookLogged) {
            interactionCycleHookLogged = true;
            System.out.println("[Viewpoint Extended Support] interaction cycling hook invoked: previousKey="
                    + previousInteractionKey + ", nextKey=" + nextInteractionKey
                    + ", aimedTarget=" + describeInteractionTarget(aimedTarget));
        }

        boolean keyPressed = previousPressed || nextPressed;
        if (debugLogging && keyPressed) {
            System.out.println("[Viewpoint Extended Support] interaction cycling key detected: key="
                    + (previousPressed ? previousInteractionKey : nextInteractionKey)
                    + ", viewEnabled=" + isViewEnabled()
                    + ", menuSuppressed=" + interactionMenuSuppressed
                    + ", autoCursor=" + autoCursorRequested
                    + ", freeCursor=" + isFreeCursor());
        }

        if (!isViewEnabled() || interactionMenuSuppressed || autoCursorRequested || isFreeCursor()) {
            if (debugLogging && keyPressed) {
                System.out.println("[Viewpoint Extended Support] interaction cycling ignored: input is suppressed");
            }
            clearInteractionTargetSelection();
            return aimedTarget;
        }

        try {
            InteractionCycleAccess access = getInteractionCycleAccess();
            if (access == null) {
                if (debugLogging && keyPressed) {
                    System.out.println("[Viewpoint Extended Support] interaction cycling ignored: target access unavailable");
                }
                return aimedTarget;
            }
            if (selectedInteractionTarget != null && cameraMovedSinceInteractionCycle(access)) {
                clearInteractionTargetSelection();
            }

            Object foundValue = access.found.get(null);
            if (!(foundValue instanceof java.util.List<?> found)) {
                if (debugLogging && keyPressed) {
                    System.out.println("[Viewpoint Extended Support] interaction cycling ignored: found targets are unavailable");
                }
                clearInteractionTargetSelection();
                return aimedTarget;
            }

            ArrayList<Object> candidates = new ArrayList<>();
            for (Object candidate : found) {
                if (!isCyclableInteractionTarget(candidate, access)) continue;
                Object object = access.targetObject.get(candidate);
                boolean duplicate = false;
                for (Object existing : candidates) {
                    if (access.targetObject.get(existing) == object) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) candidates.add(candidate);
            }

            if (debugLogging && keyPressed) {
                System.out.println("[Viewpoint Extended Support] interaction cycling candidates: count="
                        + candidates.size() + ", aimed=" + describeInteractionTarget(aimedTarget)
                        + ", list=" + describeInteractionCandidates(candidates, access));
            }

            if (selectedInteractionTarget != null
                    && findInteractionTarget(candidates, selectedInteractionTarget, access) < 0) {
                clearInteractionTargetSelection();
            }

            if ((previousPressed ^ nextPressed) && candidates.size() > 1) {
                int current = selectedInteractionTarget == null
                        ? findInteractionTarget(candidates, aimedTarget, access)
                        : findInteractionTarget(candidates, selectedInteractionTarget, access);
                int direction = nextPressed ? 1 : -1;
                int selected = Math.floorMod((current < 0 ? (direction > 0 ? -1 : 0) : current)
                        + direction, candidates.size());
                selectedInteractionTarget = access.copyTarget(candidates.get(selected));
                interactionCycleYaw = readLookAngle(access.lookYaw);
                interactionCyclePitch = readLookAngle(access.lookPitch);
                if (debugLogging) {
                    System.out.println("[Viewpoint Extended Support] interaction cycling selected: position="
                            + (selected + 1) + "/" + candidates.size()
                            + ", target=" + describeInteractionTarget(candidates.get(selected)));
                }
            } else if (debugLogging && keyPressed) {
                System.out.println("[Viewpoint Extended Support] interaction cycling ignored: fewer than two candidates");
            }

            if (selectedInteractionTarget == null) {
                selectedInteractionPosition = 0;
                selectedInteractionCount = 0;
                return aimedTarget;
            }

            int selected = findInteractionTarget(candidates, selectedInteractionTarget, access);
            if (selected < 0) {
                clearInteractionTargetSelection();
                return aimedTarget;
            }
            selectedInteractionPosition = selected + 1;
            selectedInteractionCount = candidates.size();
            return candidates.get(selected);
        } catch (Throwable failure) {
            clearInteractionTargetSelection();
            if (debugLogging && !interactionCycleFailureLogged) {
                interactionCycleFailureLogged = true;
                System.out.println("[Viewpoint Extended Support] interaction target cycling failed: "
                        + failure.getClass().getSimpleName() + ": " + failure.getMessage());
            }
            return aimedTarget;
        }
    }

    private static boolean isCyclableInteractionTarget(Object target, InteractionCycleAccess access)
            throws ReflectiveOperationException {
        return target != null
                && access.targetKind.getInt(target) != access.groundKind
                && access.targetObject.get(target) != null;
    }

    private static int findInteractionTarget(java.util.List<?> targets, Object sought,
                                             InteractionCycleAccess access)
            throws ReflectiveOperationException {
        if (sought == null) return -1;
        for (int index = 0; index < targets.size(); index++) {
            if (Boolean.TRUE.equals(access.targetSame.invoke(targets.get(index), sought))) return index;
        }
        return -1;
    }

    private static boolean cameraMovedSinceInteractionCycle(InteractionCycleAccess access)
            throws ReflectiveOperationException {
        return angleDifference(readLookAngle(access.lookYaw), interactionCycleYaw) > 0.035f
                || Math.abs(readLookAngle(access.lookPitch) - interactionCyclePitch) > 0.035f;
    }

    private static float readLookAngle(Field field) throws ReflectiveOperationException {
        return field.getFloat(null);
    }

    private static float angleDifference(float first, float second) {
        float fullTurn = (float) (Math.PI * 2.0);
        float difference = Math.abs((first - second) % fullTurn);
        return Math.min(difference, fullTurn - difference);
    }

    private static InteractionCycleAccess getInteractionCycleAccess() {
        InteractionCycleAccess access = interactionCycleAccess;
        if (access != null) return access;
        try {
            access = new InteractionCycleAccess();
            interactionCycleAccess = access;
            return access;
        } catch (ReflectiveOperationException failure) {
            if (debugLogging && !interactionCycleFailureLogged) {
                interactionCycleFailureLogged = true;
                System.out.println("[Viewpoint Extended Support] interaction target access initialization failed: "
                        + failure.getClass().getSimpleName() + ": " + failure.getMessage());
            }
            return null;
        }
    }

    private static String describeInteractionTarget(Object target) {
        return target == null ? "none" : target.getClass().getSimpleName();
    }

    private static String describeInteractionCandidates(java.util.List<?> candidates,
                                                        InteractionCycleAccess access)
            throws ReflectiveOperationException {
        StringBuilder result = new StringBuilder("[");
        for (int index = 0; index < candidates.size(); index++) {
            if (index > 0) result.append(", ");
            Object candidate = candidates.get(index);
            Object object = access.targetObject.get(candidate);
            result.append("kind=").append(access.targetKind.getInt(candidate))
                    .append(':').append(describeInteractionTarget(object));
        }
        return result.append(']').toString();
    }

    private static void clearInteractionTargetSelection() {
        selectedInteractionTarget = null;
        selectedInteractionPosition = 0;
        selectedInteractionCount = 0;
    }

    public static synchronized void setContextMenuIcon(String label, Object texture) {
        if (label != null && texture != null) {
            interactionIcons.put(label, texture);
        }
    }

    public static synchronized void setVanillaContextMenuIcon(String label, Object texture) {
        if (label != null && texture != null) {
            vanillaInteractionIcons.put(label, texture);
        }
    }

    public static void refreshContextMenuIcons() {
        InteractionMenu menu = activeInteractionMenu;
        if (menu == null) {
            return;
        }
        if (menu.grouped) {
            applyInteractionIcons(menu.rootEntries, menu.iconField, null, true);
            updateVisibleGroupIcons(menu);
        } else {
            applyFlatInteractionIcons(menu);
        }
    }

    public static int adjustContextMenuIconSpacing(Object row, int spacing, int iconSize) {
        if (row == null) {
            return spacing;
        }
        try {
            Field iconField = lootRowIcon;
            if (iconField == null) {
                iconField = field(row.getClass().getName(), "icon");
                lootRowIcon = iconField;
            }
            Field actionField = lootRowAction;
            if (actionField == null) {
                actionField = field(row.getClass().getName(), "action");
                lootRowAction = actionField;
            }
            if (iconField != null && actionField != null
                    && iconField.get(row) != null && actionField.getInt(row) >= 0) {
                return Math.max(spacing, iconSize + 8);
            }
        } catch (Throwable ignored) {
        }
        return spacing;
    }

    public static boolean shouldSkipSetupWizard() {
        return skipSetupWizard;
    }

    public static boolean shouldSkipSetupBlock(boolean original) {
        return skipSetupWizard ? false : original;
    }

    public static void setAutoCursorRequested(boolean requested) {
        requested = requested && autoCursorInUi;
        boolean changed = autoCursorRequested != requested;
        autoCursorRequested = requested;
        if (changed) {
            uiCursorCenterPending = requested && isViewEnabled();
            logDiagnosticState();
            applyCursorOverride();
            logCursorLifecycle(requested ? "ui-open" : "ui-close", true);
        }
    }

    public static void setInteractionMenuSuppressed(boolean suppressed) {
        interactionMenuSuppressed = suppressed;
    }

    public static void setInventoryOpen(boolean open) {
        inventoryOpen = open;
    }

    public static boolean shouldSkipInteractionMenu() {
        return interactionMenuSuppressed;
    }

    public static boolean shouldSkipInteractionWheel() {
        return inventoryOpen;
    }

    public static ArrayList<Float> projectCompanionDogTag(int playerNum, float x, float y, float z) {
        companionDogTagPosition.clear();
        if (!isViewEnabled() || playerNum < 0) {
            return null;
        }

        try {
            Field framesField = viewpointFrames;
            if (framesField == null) {
                framesField = field("viewpoint.FP", "frames");
                viewpointFrames = framesField;
            }
            Object frames = framesField == null ? null : framesField.get(null);
            if (frames == null || !frames.getClass().isArray()
                    || playerNum >= Array.getLength(frames)) {
                return null;
            }
            Object frame = Array.get(frames, playerNum);
            if (frame == null) {
                return null;
            }

            Field activeField = freeCameraActive;
            if (activeField == null) {
                activeField = field("viewpoint.input.FreeCam", "active");
                freeCameraActive = activeField;
            }
            Field placeField = freeCameraPlace;
            if (placeField == null) {
                placeField = field("viewpoint.input.FreeCam", "place");
                freeCameraPlace = placeField;
            }
            Object place = activeField != null && activeField.getBoolean(null)
                    && placeField != null ? placeField.get(null) : null;

            Method widthMethod = screenWidth;
            if (widthMethod == null) {
                widthMethod = method("zombie.iso.IsoCamera", "getScreenWidth", int.class);
                screenWidth = widthMethod;
            }
            Method heightMethod = screenHeight;
            if (heightMethod == null) {
                heightMethod = method("zombie.iso.IsoCamera", "getScreenHeight", int.class);
                screenHeight = heightMethod;
            }
            if (widthMethod == null || heightMethod == null) {
                return null;
            }
            float width = ((Number) widthMethod.invoke(null, playerNum)).floatValue();
            float height = ((Number) heightMethod.invoke(null, playerNum)).floatValue();

            Constructor<?> constructor = worldToScreenConstructor;
            Method setMethod = worldToScreenSet;
            Method pixelMethod = worldToScreenPixel;
            if (constructor == null || setMethod == null || pixelMethod == null) {
                Class<?> frameClass = Class.forName("viewpoint.core.Frame");
                Class<?> placeClass = Class.forName("viewpoint.input.FreeCam$Place");
                Class<?> screenClass = Class.forName("viewpoint.game.WorldToScreen");
                constructor = screenClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                setMethod = screenClass.getDeclaredMethod("set", frameClass, placeClass,
                        float.class, float.class);
                setMethod.setAccessible(true);
                pixelMethod = screenClass.getDeclaredMethod("pixel", float.class, float.class,
                        float.class, float.class, float[].class);
                pixelMethod.setAccessible(true);
                worldToScreenConstructor = constructor;
                worldToScreenSet = setMethod;
                worldToScreenPixel = pixelMethod;
            }

            Object projection = worldToScreen;
            if (projection == null) {
                projection = constructor.newInstance();
                worldToScreen = projection;
            }
            setMethod.invoke(projection, frame, place, width, height);

            if (!Boolean.TRUE.equals(pixelMethod.invoke(projection, x, y, z, 0.0f,
                    companionDogTagPixel))) {
                return null;
            }

            companionDogTagPosition.add(companionDogTagPixel[0]);
            companionDogTagPosition.add(companionDogTagPixel[1]);
            return companionDogTagPosition;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static void diagnosticTick() {
        if (!debugLogging) {
            return;
        }
        diagnosticTicks++;
        if (diagnosticTicks % 300L != 0L) {
            return;
        }

        System.out.println("[Viewpoint Extended Support] cursor diagnostics: "
                + "vanillaCursor=" + vanillaCursorHookCalls
                + ", viewpointCursor=" + viewpointCursorHookCalls
                + ", directCursorRender=" + directCursorRenderCalls
                + ", vanillaReticle=" + vanillaReticleHookCalls
                + ", viewpointReticle=" + viewpointReticleHookCalls
                + ", mouseCursor=" + mouseCursorHookCalls
                + ", lookMouseCursor=" + lookMouseCursorHookCalls
                + ", cursorTexture=" + mouseCursorTextureHookCalls
                + ", cursorVisibility=" + mouseCursorVisibilityHookCalls
                + ", cursorVisible=" + mouseCursorVisibleCalls
                + ", cursorHidden=" + mouseCursorHiddenCalls
                + ", directCursorSkipped=" + directCursorRenderSkippedCalls
                + ", view=" + isViewEnabled()
                + ", free=" + isFreeCursor()
                + ", auto=" + autoCursorRequested
                + ", third=" + isThirdPerson()
                + ", vehicle=" + isThirdPersonVehicle()
                + ", system=" + systemCursorModeName(readSystemCursorMode()));
    }

    public static void addALifeInteractionTargets(Object player, Object centerSquare) {
        if (player == null || centerSquare == null) return;

        ALifeTargetAccess access = getALifeTargetAccess();
        if (access == null) return;

        try {
            Object world = access.worldInstance.get(null);
            Object cell = world == null ? null : access.currentCell.get(world);
            if (cell == null) return;

            int centerX = access.squareX.getInt(centerSquare);
            int centerY = access.squareY.getInt(centerSquare);
            int centerZ = access.squareZ.getInt(centerSquare);
            if (debugLogging && !alifeTargetHookLogged) {
                alifeTargetHookLogged = true;
                System.out.println("[Viewpoint Extended Support] A-Life interaction target hook active; scanning visible NPC shells");
            }

            int added = 0;
            for (int y = -2; y <= 2; y++) {
                for (int x = -2; x <= 2; x++) {
                    Object square = access.getGridSquare.invoke(cell,
                            centerX + x, centerY + y, centerZ);
                    if (square == null) continue;

                    Object movingObjects = access.getMovingObjects.invoke(square);
                    if (!(movingObjects instanceof java.util.List<?> objects)) continue;

                    for (Object candidate : objects) {
                        if (!access.zombieClass.isInstance(candidate)
                                || Boolean.TRUE.equals(access.isDead.invoke(candidate))
                                || !isALifeNpc(candidate, access)
                                || !Boolean.TRUE.equals(access.canSee.invoke(player, candidate))) {
                            continue;
                        }

                        float targetX = ((Number) access.getX.invoke(candidate)).floatValue();
                        float targetY = ((Number) access.getY.invoke(candidate)).floatValue();
                        float targetZ = ((Number) access.getZ.invoke(candidate)).floatValue()
                                * 2.4494896f;
                        int targetIndex = ((Number) access.addPersonTarget.invoke(null,
                                access.personTargetKind.getInt(null), null, candidate)).intValue();
                        if (targetIndex < 0) continue;

                        access.growTargetBox.invoke(null, targetIndex,
                                targetX - 0.3f, targetY - 0.3f, targetZ,
                                targetX + 0.3f, targetY + 0.3f, targetZ + 1.6f);
                        added++;
                    }
                }
            }

            if (added > 0 && debugLogging && !alifeTargetAddedLogged) {
                alifeTargetAddedLogged = true;
                System.out.println("[Viewpoint Extended Support] added " + added
                        + " visible A-Life NPC(s) to Viewpoint interaction targets");
            }
        } catch (Throwable failure) {
            if (debugLogging && !alifeTargetFailureLogged) {
                alifeTargetFailureLogged = true;
                System.out.println("[Viewpoint Extended Support] A-Life interaction target bridge failed: "
                        + failure.getClass().getSimpleName() + ": " + failure.getMessage());
            }
        }
    }

    public static Object getAimedALifeNpc() {
        if (!isViewEnabled() || shouldSkipInteractionMenu()) return null;

        ALifeTargetAccess access = getALifeTargetAccess();
        if (access == null) return null;

        try {
            Object target = access.aimedInteractionObject.get(null);
            if (target == null || !access.zombieClass.isInstance(target)
                    || Boolean.TRUE.equals(access.isDead.invoke(target))
                    || !isALifeNpc(target, access)) {
                return null;
            }
            return target;
        } catch (Throwable failure) {
            if (debugLogging && !alifeAimTargetFailureLogged) {
                alifeAimTargetFailureLogged = true;
                System.out.println("[Viewpoint Extended Support] A-Life aimed target lookup failed: "
                        + failure.getClass().getSimpleName() + ": " + failure.getMessage());
            }
            return null;
        }
    }

    public static Object getAimedInteractionObject() {
        if (!isViewEnabled() || shouldSkipInteractionMenu()) return null;

        ALifeTargetAccess access = getALifeTargetAccess();
        if (access == null) return null;

        try {
            return access.aimedInteractionObject.get(null);
        } catch (Throwable failure) {
            if (debugLogging && !alifeAimTargetFailureLogged) {
                alifeAimTargetFailureLogged = true;
                System.out.println("[Viewpoint Extended Support] Viewpoint aimed target lookup failed: "
                        + failure.getClass().getSimpleName() + ": " + failure.getMessage());
            }
            return null;
        }
    }

    private static boolean isALifeNpc(Object candidate, ALifeTargetAccess access) throws Exception {
        Object modData = access.getModData.invoke(candidate);
        if (modData == null) return false;
        Object uid = access.getModDataValue.invoke(modData, "ProjectALifeUID");
        return uid != null && !uid.toString().isEmpty();
    }

    private static ALifeTargetAccess getALifeTargetAccess() {
        ALifeTargetAccess access = alifeTargetAccess;
        if (access != null) return access;

        synchronized (Bridge.class) {
            access = alifeTargetAccess;
            if (access != null) return access;
            if (alifeTargetAccessWarningLogged) return null;

            try {
                access = new ALifeTargetAccess();
                alifeTargetAccess = access;
                return access;
            } catch (Throwable failure) {
                alifeTargetAccessWarningLogged = true;
                if (debugLogging) {
                    System.out.println("[Viewpoint Extended Support] A-Life interaction target bridge unavailable: "
                            + failure.getClass().getSimpleName() + ": " + failure.getMessage());
                }
                return null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void groupContextMenuActions(Object rowsObject) {
        if (rowsObject == null) {
            return;
        }

        InteractionMenu menu = null;
        try {
            InteractionMenu previous = activeInteractionMenu;
            if (previous != null) {
                restoreInteractionMenu(previous);
                activeInteractionMenu = null;
            }

            Field rowsField = lootRows;
            if (rowsField == null) {
                rowsField = field("viewpoint.interact.LootRows", "rows");
                lootRows = rowsField;
            }
            if (rowsField == null) {
                return;
            }

            Object rowsValue = rowsField.get(rowsObject);
            if (!(rowsValue instanceof ArrayList<?>)) {
                return;
            }
            ArrayList<Object> rows = (ArrayList<Object>) rowsValue;

            Class<?> rowClass = Class.forName("viewpoint.interact.LootRows$Row");
            Field nameField = lootRowName;
            Field actionField = lootRowAction;
            Field iconField = lootRowIcon;
            Field enabledField = lootRowEnabled;
            Field selectedField = lootRowsSelected;
            Constructor<?> rowFactory = lootRowConstructor;
            if (nameField == null) {
                nameField = field(rowClass.getName(), "name");
                lootRowName = nameField;
            }
            if (actionField == null) {
                actionField = field(rowClass.getName(), "action");
                lootRowAction = actionField;
            }
            if (iconField == null) {
                iconField = field(rowClass.getName(), "icon");
                lootRowIcon = iconField;
            }
            if (enabledField == null) {
                enabledField = field(rowClass.getName(), "enabled");
                lootRowEnabled = enabledField;
            }
            if (selectedField == null) {
                selectedField = field("viewpoint.interact.LootRows", "selected");
                lootRowsSelected = selectedField;
            }
            if (rowFactory == null) {
                rowFactory = rowClass.getDeclaredConstructor();
                rowFactory.setAccessible(true);
                lootRowConstructor = rowFactory;
            }
            if (nameField == null || actionField == null || iconField == null
                    || enabledField == null || selectedField == null) {
                return;
            }

            int firstAction = rows.size();
            while (firstAction > 0 && actionField.getInt(rows.get(firstAction - 1)) >= 0) {
                firstAction--;
            }
            if (firstAction == rows.size()) {
                return;
            }

            menu = new InteractionMenu(rowsObject, rows, selectedField,
                    nameField, actionField, enabledField, rowFactory);
            menu.prefixRows.addAll(rows.subList(0, firstAction));
            for (int index = firstAction; index < rows.size(); index++) {
                Object row = rows.get(index);
                String name = (String) nameField.get(row);
                InteractionAction interactionAction = new InteractionAction(row, name);
                menu.actionRows.add(interactionAction);
                addRootInteractionAction(menu, interactionAction);
            }

            menu.iconField = iconField;
            menu.grouped = groupContextMenuActionsEnabled && containsGroup(menu.rootEntries);
            if (menu.grouped) {
                assignGroupActions(menu, menu.rootEntries);
            }

            boolean hasIcons = menu.grouped
                    ? applyInteractionIcons(menu.rootEntries, iconField, null, false)
                    : applyFlatInteractionIcons(menu);
            if (!menu.grouped && !hasIcons) {
                return;
            }

            activeInteractionMenu = menu;
            if (menu.grouped) {
                showInteractionLevel(menu, true);
            }
        } catch (Throwable failure) {
            if (menu != null) {
                restoreInteractionMenu(menu);
                if (activeInteractionMenu == menu) {
                    activeInteractionMenu = null;
                }
            }
            logContextGroupingFailure(failure);
        }
    }

    public static boolean handleGroupedContextAction(int action) {
        InteractionMenu menu = activeInteractionMenu;
        if (!groupContextMenuActionsEnabled || menu == null) {
            return false;
        }

        try {
            if (action == GROUP_BACK_ACTION) {
                if (menu.path.isEmpty()) {
                    return false;
                }
                menu.path.remove(menu.path.size() - 1);
                int previousSelection = menu.parentSelections.remove(menu.parentSelections.size() - 1);
                showInteractionLevel(menu, false);
                menu.selectedField.setInt(menu.rowsObject, previousSelection);
                return true;
            }

            InteractionEntry group = menu.groupsByAction.get(action);
            if (group == null) {
                return false;
            }
            menu.parentSelections.add(menu.selectedField.getInt(menu.rowsObject));
            menu.path.add(group);
            showInteractionLevel(menu, true);
            return true;
        } catch (Throwable failure) {
            logContextGroupingFailure(failure);
            return true;
        }
    }

    public static void clearGroupedContextMenu(Object rowsObject) {
        InteractionMenu menu = activeInteractionMenu;
        if (menu == null || menu.rowsObject != rowsObject) {
            return;
        }
        restoreInteractionMenu(menu);
        activeInteractionMenu = null;
    }

    private static void addRootInteractionAction(InteractionMenu menu, InteractionAction action) {
        String label = action.originalName;
        int separator = label == null ? -1 : label.indexOf(": ");
        if (separator < 0) {
            menu.rootEntries.add(InteractionEntry.action(label, action));
            return;
        }

        String groupName = label.substring(0, separator);
        findOrCreateGroup(menu.rootEntries, groupName, groupName).candidateActions.add(action);
    }

    private static InteractionEntry findOrCreateGroup(ArrayList<InteractionEntry> entries,
                                                      String name, String path) {
        for (InteractionEntry entry : entries) {
            if (entry.group && entry.path.equals(path)) {
                return entry;
            }
        }
        InteractionEntry group = InteractionEntry.group(name, path);
        entries.add(group);
        return group;
    }

    private static void populateGroupChildren(InteractionMenu menu, InteractionEntry parent) {
        if (parent.childrenLoaded) {
            return;
        }

        String prefix = parent.path + ": ";
        for (InteractionAction action : parent.candidateActions) {
            String label = action.originalName;
            if (label == null || !label.startsWith(prefix)) {
                continue;
            }

            String childLabel = label.substring(prefix.length());
            int separator = childLabel.indexOf(": ");
            if (separator < 0) {
                parent.children.add(InteractionEntry.action(childLabel, action));
            } else {
                String childName = childLabel.substring(0, separator);
                String childPath = prefix + childName;
                findOrCreateGroup(parent.children, childName, childPath).candidateActions.add(action);
            }
        }
        parent.childrenLoaded = true;
    }

    private static boolean containsGroup(ArrayList<InteractionEntry> entries) {
        for (InteractionEntry entry : entries) {
            if (entry.group) {
                return true;
            }
        }
        return false;
    }

    private static void assignGroupActions(InteractionMenu menu, ArrayList<InteractionEntry> entries) {
        for (InteractionEntry entry : entries) {
            if (entry.group && entry.action == 0) {
                entry.action = GROUP_ACTION_START + menu.groupsByAction.size();
                menu.groupsByAction.put(entry.action, entry);
            }
        }
    }

    private static void showInteractionLevel(InteractionMenu menu, boolean selectFirst) throws Exception {
        menu.rows.clear();
        menu.rows.addAll(menu.prefixRows);
        ArrayList<InteractionEntry> visibleEntries = menu.path.isEmpty()
                ? menu.rootEntries : menu.path.get(menu.path.size() - 1).children;
        if (!menu.path.isEmpty()) {
            InteractionEntry parent = menu.path.get(menu.path.size() - 1);
            populateGroupChildren(menu, parent);
            assignGroupActions(menu, visibleEntries);
            applyInteractionIcons(visibleEntries, menu.iconField, parent.icon, false);
        }

        int firstSelectable = -1;
        int backIndex = -1;
        if (!menu.path.isEmpty()) {
            backIndex = menu.rows.size();
            menu.rows.add(createInteractionRow(menu, "< Back", GROUP_BACK_ACTION, null));
        }
        for (InteractionEntry entry : visibleEntries) {
            Object row = entry.group
                    ? createInteractionRow(menu, entry.name + "  >", entry.action, entry.icon)
                    : entry.interactionAction.row;
            menu.rows.add(row);
            if (!entry.group && (entry.name == null
                    ? entry.interactionAction.originalName != null
                    : !entry.name.equals(entry.interactionAction.originalName))) {
                menu.nameField.set(row, entry.name);
            }
            if (firstSelectable < 0 && isInteractionRowSelectable(menu, row)) {
                firstSelectable = menu.rows.size() - 1;
            }
        }
        if (selectFirst) {
            if (firstSelectable < 0) {
                firstSelectable = backIndex;
            }
            if (firstSelectable >= 0) {
                menu.selectedField.setInt(menu.rowsObject, firstSelectable);
            }
        }
    }

    private static boolean isInteractionRowSelectable(InteractionMenu menu, Object row) throws IllegalAccessException {
        return menu.actionField.getInt(row) >= 0 && menu.enabledField.getBoolean(row);
    }

    private static Object createInteractionRow(InteractionMenu menu, String name, int action, Object icon) throws Exception {
        Object row = menu.rowFactory.newInstance();
        menu.nameField.set(row, name);
        menu.actionField.setInt(row, action);
        menu.iconField.set(row, icon);
        menu.enabledField.setBoolean(row, true);
        return row;
    }

    private static void restoreInteractionMenu(InteractionMenu menu) {
        try {
            for (InteractionAction action : menu.actionRows) {
                menu.nameField.set(action.row, action.originalName);
                if (action.originalIconCaptured) {
                    menu.iconField.set(action.row, action.originalIcon);
                }
            }
            menu.rows.clear();
            menu.rows.addAll(menu.prefixRows);
            for (InteractionAction action : menu.actionRows) {
                menu.rows.add(action.row);
            }
            menu.path.clear();
            menu.parentSelections.clear();
            if (!menu.rows.isEmpty()) {
                int selected = Math.max(0, Math.min(menu.selectedField.getInt(menu.rowsObject), menu.rows.size() - 1));
                menu.selectedField.setInt(menu.rowsObject, selected);
            }
        } catch (Throwable failure) {
            logContextGroupingFailure(failure);
        }
    }

    private static boolean applyInteractionIcons(ArrayList<InteractionEntry> entries, Field iconField,
                                                 Object parentIcon, boolean recursive) {
        boolean found = false;
        try {
            for (InteractionEntry entry : entries) {
                if (entry.group) {
                    entry.icon = vanillaContextMenuIcon(entry.name);
                    if (entry.icon == null) {
                        entry.icon = contextMenuIcon(entry.name);
                    }
                    if (entry.icon == null) {
                        entry.icon = parentIcon;
                    }
                    if (entry.icon != null) {
                        found = true;
                    }
                    if (recursive) {
                        found |= applyInteractionIcons(entry.children, iconField, entry.icon, true);
                    }
                    continue;
                }

                InteractionAction action = entry.interactionAction;
                if (!action.originalIconCaptured) {
                    action.originalIcon = iconField.get(action.row);
                    action.originalIconCaptured = true;
                }
                Object icon = vanillaContextMenuIcon(action.originalName);
                if (icon == null) {
                    icon = vanillaContextMenuIcon(entry.name);
                }
                if (icon == null) {
                    icon = contextMenuIcon(action.originalName);
                }
                if (icon == null) {
                    icon = contextMenuIcon(entry.name);
                }
                if (icon == null) {
                    icon = parentIcon;
                }
                if (icon != null && iconField.getType().isInstance(icon)) {
                    iconField.set(action.row, icon);
                    found = true;
                } else {
                    iconField.set(action.row, action.originalIcon);
                }
            }
        } catch (Throwable failure) {
            logContextGroupingFailure(failure);
        }
        return found;
    }

    private static boolean applyFlatInteractionIcons(InteractionMenu menu) {
        boolean found = false;
        for (InteractionAction action : menu.actionRows) {
            if (!action.originalIconCaptured) {
                try {
                    action.originalIcon = menu.iconField.get(action.row);
                    action.originalIconCaptured = true;
                } catch (IllegalAccessException failure) {
                    logContextGroupingFailure(failure);
                    continue;
                }
            }

            Object icon = vanillaContextMenuIcon(action.originalName);
            if (icon == null) icon = contextMenuIcon(action.originalName);
            int separator = action.originalName == null ? -1 : action.originalName.indexOf(": ");
            if (icon == null && separator >= 0) {
                String groupName = action.originalName.substring(0, separator);
                icon = vanillaContextMenuIcon(groupName);
                if (icon == null) icon = contextMenuIcon(groupName);
            }
            try {
                if (icon != null && menu.iconField.getType().isInstance(icon)) {
                    menu.iconField.set(action.row, icon);
                    found = true;
                } else {
                    menu.iconField.set(action.row, action.originalIcon);
                }
            } catch (IllegalAccessException failure) {
                logContextGroupingFailure(failure);
            }
        }
        return found;
    }

    private static void updateVisibleGroupIcons(InteractionMenu menu) {
        try {
            for (Object row : menu.rows) {
                int action = menu.actionField.getInt(row);
                if (action == GROUP_BACK_ACTION) {
                    menu.iconField.set(row, null);
                    continue;
                }
                InteractionEntry group = menu.groupsByAction.get(action);
                if (group != null) {
                    menu.iconField.set(row, group.icon);
                }
            }
        } catch (Throwable failure) {
            logContextGroupingFailure(failure);
        }
    }

    private static Object contextMenuIcon(String label) {
        if (label == null) {
            return null;
        }
        synchronized (interactionIcons) {
            return interactionIcons.get(label);
        }
    }

    private static Object vanillaContextMenuIcon(String label) {
        if (label == null) {
            return null;
        }
        synchronized (vanillaInteractionIcons) {
            return vanillaInteractionIcons.get(label);
        }
    }

    private static void logContextGroupingFailure(Throwable failure) {
        if (debugLogging && !contextGroupingWarningLogged) {
            contextGroupingWarningLogged = true;
            System.out.println("[Viewpoint Extended Support] could not update interaction menu: "
                    + failure.getClass().getSimpleName());
        }
    }

    private static Field requiredField(Class<?> type, String name) throws ReflectiveOperationException {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static Method requiredMethod(Class<?> type, String name, Class<?>... parameterTypes)
            throws ReflectiveOperationException {
        Method method = type.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    private static Method requiredPublicMethod(Class<?> type, String name, Class<?>... parameterTypes)
            throws ReflectiveOperationException {
        Method method = type.getMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }

    private static final class ALifeTargetAccess {
        private final Class<?> zombieClass;
        private final Field worldInstance;
        private final Field currentCell;
        private final Field squareX;
        private final Field squareY;
        private final Field squareZ;
        private final Field personTargetKind;
        private final Method getGridSquare;
        private final Method getMovingObjects;
        private final Method isDead;
        private final Method getModData;
        private final Method getModDataValue;
        private final Method canSee;
        private final Method getX;
        private final Method getY;
        private final Method getZ;
        private final Field aimedInteractionObject;
        private final Method addPersonTarget;
        private final Method growTargetBox;

        private ALifeTargetAccess() throws ReflectiveOperationException {
            Class<?> worldClass = Class.forName("zombie.iso.IsoWorld");
            Class<?> cellClass = Class.forName("zombie.iso.IsoCell");
            Class<?> squareClass = Class.forName("zombie.iso.IsoGridSquare");
            Class<?> movingObjectClass = Class.forName("zombie.iso.IsoMovingObject");
            Class<?> playerClass = Class.forName("zombie.characters.IsoPlayer");
            Class<?> isoObjectClass = Class.forName("zombie.iso.IsoObject");
            Class<?> lootTargetsClass = Class.forName("viewpoint.interact.LootTargets");
            Class<?> lootBoxesClass = Class.forName("viewpoint.interact.LootBoxes");
            Class<?> interactActionsClass = Class.forName("viewpoint.interact.InteractActions");
            Class<?> kahluaTableClass = Class.forName("se.krka.kahlua.vm.KahluaTable");

            zombieClass = Class.forName("zombie.characters.IsoZombie");
            worldInstance = requiredField(worldClass, "instance");
            currentCell = requiredField(worldClass, "currentCell");
            squareX = requiredField(squareClass, "x");
            squareY = requiredField(squareClass, "y");
            squareZ = requiredField(squareClass, "z");
            personTargetKind = requiredField(lootTargetsClass, "PERSON");
            getGridSquare = requiredPublicMethod(cellClass, "getGridSquare",
                    int.class, int.class, int.class);
            getMovingObjects = requiredPublicMethod(squareClass, "getMovingObjects");
            isDead = requiredPublicMethod(zombieClass, "isDead");
            getModData = requiredPublicMethod(zombieClass, "getModData");
            getModDataValue = requiredPublicMethod(kahluaTableClass, "rawget", Object.class);
            canSee = requiredPublicMethod(playerClass, "CanSee", movingObjectClass);
            getX = requiredPublicMethod(zombieClass, "getX");
            getY = requiredPublicMethod(zombieClass, "getY");
            getZ = requiredPublicMethod(zombieClass, "getZ");
            aimedInteractionObject = requiredField(interactActionsClass, "aimed");
            addPersonTarget = requiredMethod(lootTargetsClass, "add",
                    int.class, squareClass, isoObjectClass);
            growTargetBox = requiredMethod(lootBoxesClass, "grow", int.class,
                    float.class, float.class, float.class,
                    float.class, float.class, float.class);
        }
    }

    private static final class InteractionCycleAccess {
        private final Field found;
        private final Field targetKind;
        private final Field targetObject;
        private final int groundKind;
        private final Method targetSame;
        private final Constructor<?> targetConstructor;
        private final Method targetSet;
        private final Field lookYaw;
        private final Field lookPitch;

        private InteractionCycleAccess() throws ReflectiveOperationException {
            Class<?> targetsClass = Class.forName("viewpoint.interact.LootTargets");
            Class<?> targetClass = Class.forName("viewpoint.interact.LootTargets$Target");
            Class<?> lookClass = Class.forName("viewpoint.input.Look");
            found = requiredField(targetsClass, "found");
            targetKind = requiredField(targetClass, "kind");
            targetObject = requiredField(targetClass, "object");
            groundKind = requiredField(targetsClass, "GROUND").getInt(null);
            targetSame = requiredMethod(targetClass, "same", targetClass);
            targetConstructor = targetClass.getDeclaredConstructor();
            targetConstructor.setAccessible(true);
            targetSet = requiredMethod(targetClass, "set", targetClass);
            lookYaw = requiredField(lookClass, "yaw");
            lookPitch = requiredField(lookClass, "pitch");
        }

        private Object copyTarget(Object target) throws ReflectiveOperationException {
            Object copy = targetConstructor.newInstance();
            targetSet.invoke(copy, target);
            return copy;
        }
    }

    private static final class InteractionMenu {
        private final Object rowsObject;
        private final ArrayList<Object> rows;
        private final ArrayList<Object> prefixRows = new ArrayList<>();
        private final ArrayList<InteractionAction> actionRows = new ArrayList<>();
        private final ArrayList<InteractionEntry> rootEntries = new ArrayList<>();
        private final LinkedHashMap<Integer, InteractionEntry> groupsByAction = new LinkedHashMap<>();
        private final ArrayList<InteractionEntry> path = new ArrayList<>();
        private final ArrayList<Integer> parentSelections = new ArrayList<>();
        private final Field selectedField;
        private final Field nameField;
        private final Field actionField;
        private Field iconField;
        private final Field enabledField;
        private final Constructor<?> rowFactory;
        private boolean grouped;

        private InteractionMenu(Object rowsObject, ArrayList<Object> rows,
                                Field selectedField, Field nameField, Field actionField,
                                Field enabledField, Constructor<?> rowFactory) {
            this.rowsObject = rowsObject;
            this.rows = rows;
            this.selectedField = selectedField;
            this.nameField = nameField;
            this.actionField = actionField;
            this.enabledField = enabledField;
            this.rowFactory = rowFactory;
        }
    }

    private static final class InteractionEntry {
        private final String name;
        private final boolean group;
        private final InteractionAction interactionAction;
        private final String path;
        private final ArrayList<InteractionEntry> children = new ArrayList<>();
        private final ArrayList<InteractionAction> candidateActions = new ArrayList<>();
        private boolean childrenLoaded;
        private Object icon;
        private int action;

        private InteractionEntry(String name, boolean group, InteractionAction interactionAction, String path) {
            this.name = name;
            this.group = group;
            this.interactionAction = interactionAction;
            this.path = path;
        }

        private static InteractionEntry group(String name, String path) {
            return new InteractionEntry(name, true, null, path);
        }

        private static InteractionEntry action(String name, InteractionAction action) {
            return new InteractionEntry(name, false, action, null);
        }
    }

    private static final class InteractionAction {
        private final Object row;
        private final String originalName;
        private Object originalIcon;
        private boolean originalIconCaptured;

        private InteractionAction(Object row, String originalName) {
            this.row = row;
            this.originalName = originalName;
        }
    }

    public static boolean isViewEnabled() {
        return getBoolean(viewEnabled, "viewpoint.core.View", "enabled");
    }

    public static boolean isThirdPerson() {
        return getBoolean(thirdPersonActive, "viewpoint.input.ThirdPerson", "active");
    }

    public static void setThirdPerson(boolean enabled) {
        setBoolean(thirdPersonActive, "viewpoint.input.ThirdPerson", "active", enabled);
    }

    public static void setViewEnabled(boolean enabled) {
        setBoolean(viewEnabled, "viewpoint.core.View", "enabled", enabled);
    }

    public static boolean enableViewpoint() {
        return isViewEnabled() || toggleView();
    }

    public static boolean isFreeCursor() {
        return getBoolean(cursorMode, "viewpoint.FP", "cursorMode") || freeCursorOverrideActive;
    }

    public static void prepareMouseCursorUpdate() {
        if (!isViewEnabled()) {
            uiCursorCenterPending = false;
        } else if (autoCursorRequested || freeCursorOverrideActive) {
            setBoolean(lookWantCapture, "viewpoint.input.Look", "wantCapture", false);
        }
        logCursorLifecycle("look-enter", false);
    }

    public static void prepareCursorCaptureRequest() {
        cursorCaptureModeOverridden = isViewEnabled()
                && (autoCursorRequested || freeCursorOverrideActive)
                && !getBoolean(cursorMode, "viewpoint.FP", "cursorMode");
        if (cursorCaptureModeOverridden) {
            // FP.viewInput publishes wantCapture on a different thread from Look's cursor update.
            setBoolean(cursorMode, "viewpoint.FP", "cursorMode", true);
        }
    }

    public static void finishCursorCaptureRequest() {
        if (cursorCaptureModeOverridden) {
            setBoolean(cursorMode, "viewpoint.FP", "cursorMode", false);
            cursorCaptureModeOverridden = false;
        }
    }

    public static boolean shouldSkipVanillaCursor(boolean original) {
        recordHook(1, "Hooks.skipIsoCursor", original);
        if (!isViewEnabled()) {
            return original;
        }
        return true;
    }

    public static boolean shouldSkipViewpointCursor(boolean original) {
        recordHook(2, "Patch_IsoCursor.enter", original);
        if (!isViewEnabled()) {
            return original;
        }
        return true;
    }

    public static boolean shouldSkipCursorRender() {
        recordHook(3, "IsoCursor.render", true);
        boolean skip = isViewEnabled();
        if (skip && debugLogging) {
            directCursorRenderSkippedCalls++;
            if (directCursorRenderSkippedCalls == 1L) {
                System.out.println("[Viewpoint Extended Support] cursor render decision: skip=true");
            }
        }
        return skip;
    }

    public static boolean shouldSkipVanillaReticle(boolean original) {
        recordHook(4, "Hooks.skipIsoReticle", original);
        if (!isViewEnabled()) {
            return original;
        }
        return isFreeCursor() || isThirdPersonVehicle();
    }

    public static boolean shouldSkipViewpointReticle(boolean original) {
        recordHook(5, "Patch_IsoReticle.enter", original);
        if (!isViewEnabled()) {
            return original;
        }
        return isFreeCursor() || isThirdPersonVehicle();
    }

    public static boolean shouldSkipViewpointCrosshair() {
        return isViewEnabled() && (isFreeCursor() || isThirdPersonVehicle());
    }

    public static boolean overrideMouseCursorUpdate(boolean original) {
        recordHook(6, "Hooks.skipMouseCursorUpdate", original);
        return overrideMouseCursorUpdateInternal(original);
    }

    public static boolean overrideLookMouseCursorUpdate(boolean original) {
        recordHook(7, "Look.onUpdateMouseCursor", original);
        boolean centered = !original && uiCursorCenterPending && autoCursorRequested
                && isViewEnabled() && readSystemCursorMode() == GLFW_CURSOR_NORMAL
                && centerSystemCursor();
        if (centered) {
            uiCursorCenterPending = false;
            synchronizeUiMousePosition();
        }
        logCursorLifecycle("look-exit centered=" + centered, centered);
        return overrideMouseCursorUpdateInternal(original);
    }

    public static void synchronizeUiMousePosition() {
        if (!autoCursorRequested || !isViewEnabled()
                || getBoolean(null, "viewpoint.input.Look", "captured")) {
            return;
        }

        int cursorMode = readSystemCursorMode();
        if (cursorMode != GLFW_CURSOR_NORMAL && cursorMode != GLFW_CURSOR_HIDDEN) {
            return;
        }

        try {
            if (getDisplayWindow == null) {
                getDisplayWindow = method("org.lwjglx.opengl.Display", "getWindow");
            }
            if (getDisplayHeight == null) {
                getDisplayHeight = method("org.lwjglx.opengl.Display", "getHeight");
            }
            if (glfwGetCursorPos == null) {
                glfwGetCursorPos = method("org.lwjgl.glfw.GLFW", "glfwGetCursorPos",
                        long.class, DoubleBuffer.class, DoubleBuffer.class);
            }
            if (getCoreInstance == null) {
                getCoreInstance = method("zombie.core.Core", "getInstance");
            }
            if (coreGetScreenHeight == null) {
                coreGetScreenHeight = method("zombie.core.Core", "getScreenHeight");
            }
            if (gameMouseX == null) {
                gameMouseX = field("zombie.input.Mouse", "x");
            }
            if (gameMouseY == null) {
                gameMouseY = field("zombie.input.Mouse", "y");
            }
            if (getDisplayWindow == null || getDisplayHeight == null || glfwGetCursorPos == null
                    || getCoreInstance == null || coreGetScreenHeight == null
                    || gameMouseX == null || gameMouseY == null) {
                logUiMousePositionSyncFailure("mouse coordinate access unavailable");
                return;
            }

            long window = ((Number) getDisplayWindow.invoke(null)).longValue();
            if (window == 0L) {
                return;
            }

            cursorXBuffer.clear();
            cursorYBuffer.clear();
            glfwGetCursorPos.invoke(null, window, cursorXBuffer, cursorYBuffer);
            Object core = getCoreInstance.invoke(null);
            int screenHeight = ((Number) coreGetScreenHeight.invoke(core)).intValue();
            int displayHeight = ((Number) getDisplayHeight.invoke(null)).intValue();
            int mouseX = (int) cursorXBuffer.get(0);
            int mouseY = screenHeight - displayHeight + (int) cursorYBuffer.get(0) - 1;
            synchronized (gameMouseX.getDeclaringClass()) {
                gameMouseX.setInt(null, mouseX);
                gameMouseY.setInt(null, mouseY);
            }
        } catch (Throwable error) {
            logUiMousePositionSyncFailure(error.getClass().getSimpleName());
        }
    }

    public static boolean shouldSkipMouseCursorTexture() {
        recordHook(8, "Mouse.renderCursorTexture", true);
        return isViewEnabled();
    }

    public static boolean overrideMouseCursorVisibility(boolean original) {
        recordHook(9, "Mouse.isCursorVisible", original);
        if (!isViewEnabled()) {
            return original;
        }
        boolean vehicleCamera = isThirdPersonVehicle();
        boolean freeCursor = isFreeCursor() && !vehicleCamera;
        boolean mouseCaptured = readSystemCursorMode() == GLFW_CURSOR_DISABLED;
        boolean visible = autoCursorRequested
                || (!vehicleCamera && (freeCursor || (!mouseCaptured && original)));
        if (debugLogging) {
            if (visible) {
                mouseCursorVisibleCalls++;
            } else {
                mouseCursorHiddenCalls++;
            }
        }
        return visible;
    }

    private static boolean overrideMouseCursorUpdateInternal(boolean original) {
        if (isViewEnabled() && isFreeCursor()) {
            return original;
        }

        if (!original && systemCursorHidden) {
            setSystemCursorMode(GLFW_CURSOR_NORMAL);
        }
        return original;
    }

    public static boolean isThirdPersonVehicle() {
        return isThirdPerson() && getCameraVehicle() != null;
    }

    public static void noteLootCursor(boolean requested) {
        if (requested) {
            lootCursorRequested = true;
        }
    }

    public static void applyCursorOverride() {
        if (!autoCursorInUi) {
            autoCursorRequested = false;
            uiCursorCenterPending = false;
        }
        if (!holdFreeCursor && !autoCursorInUi) {
            autoCursorRequested = false;
            lootCursorRequested = false;
            setFreeCursorOverrideActive(false);
            if (forcedCursor) {
                setCursorMode(false);
                forcedCursor = false;
            }
            return;
        }

        boolean lootCursor = lootCursorRequested;
        lootCursorRequested = false;
        boolean settingsCursor = callBoolean("viewpoint.platform.SettingsWindow", "shown");
        boolean paused = callBoolean("zombie.GameTime", "isGamePaused");

        if (holdFreeCursor) {
            boolean held = freeCursorKey > 0
                    && isDownWithModifiers(freeCursorKey, freeCursorModifiers);
            setFreeCursorOverrideActive(held || autoCursorRequested || lootCursor || settingsCursor || paused);
            return;
        }

        setFreeCursorOverrideActive(false);
        if (autoCursorRequested) {
            setCursorMode(true);
            forcedCursor = true;
        } else if (forcedCursor && !lootCursor && !settingsCursor && !paused) {
            setCursorMode(false);
            forcedCursor = false;
        }
    }

    private static boolean toggleView() {
        Object player = callObject("zombie.iso.IsoCamera", "getCameraCharacter");
        if (player == null) {
            return false;
        }

        Boolean supported = callBooleanObject("viewpoint.platform.BuildPin", "supported");
        if (Boolean.FALSE.equals(supported)) {
            call("viewpoint.platform.BuildPin", "reportRefused");
            return false;
        }

        boolean enabled = !isViewEnabled();
        setViewEnabled(enabled);
        setCursorMode(false);
        call("viewpoint.FP", "resetCaches");
        setLook(player);

        if (enabled) {
            call("viewpoint.Compat", "check");
        }
        if (debugLogging) {
            System.out.println("[Viewpoint Extended Support] first person "
                    + (enabled ? "on" : "off"));
        }
        return isViewEnabled() == enabled;
    }

    private static void setLook(Object player) {
        try {
            Method direction = player.getClass().getMethod("getDirectionAngleRadians");
            setFloat(lookYaw, "viewpoint.input.Look", "yaw", ((Number) direction.invoke(player)).floatValue());
            setFloat(lookPitch, "viewpoint.input.Look", "pitch", 0.0f);
        } catch (Throwable ignored) {
        }
    }

    private static boolean isDown(int key) {
        try {
            if (keyDown == null) {
                keyDown = method("zombie.input.GameKeyboard", "isKeyDown", int.class);
                if (debugLogging && !interactionCycleKeyboardLogged) {
                    interactionCycleKeyboardLogged = true;
                    System.out.println("[Viewpoint Extended Support] interaction cycling keyboard query: available="
                            + (keyDown != null));
                }
            }
            return keyDown != null && (Boolean) keyDown.invoke(null, key);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isDownWithModifiers(int key, int modifiers) {
        return isDown(key) && modifiersMatch(key, modifiers);
    }

    private static boolean modifiersMatch(int key, int modifiers) {
        boolean shift = (key != LEFT_SHIFT_KEY && key != RIGHT_SHIFT_KEY)
                && (isDown(LEFT_SHIFT_KEY) || isDown(RIGHT_SHIFT_KEY));
        boolean control = (key != LEFT_CONTROL_KEY && key != RIGHT_CONTROL_KEY)
                && (isDown(LEFT_CONTROL_KEY) || isDown(RIGHT_CONTROL_KEY));
        boolean alt = (key != LEFT_ALT_KEY && key != RIGHT_ALT_KEY)
                && (isDown(LEFT_ALT_KEY) || isDown(RIGHT_ALT_KEY));
        return shift == ((modifiers & MODIFIER_SHIFT) != 0)
                && control == ((modifiers & MODIFIER_CONTROL) != 0)
                && alt == ((modifiers & MODIFIER_ALT) != 0);
    }


    private static void setCursorMode(boolean enabled) {
        boolean previous = getBoolean(cursorMode, "viewpoint.FP", "cursorMode");
        setBoolean(cursorMode, "viewpoint.FP", "cursorMode", enabled);
        if (previous != enabled) {
            logDiagnosticState();
        }
    }

    private static void setFreeCursorOverrideActive(boolean active) {
        if (freeCursorOverrideActive == active) {
            return;
        }
        freeCursorOverrideActive = active;
        logDiagnosticState();
    }

    private static boolean setSystemCursorMode(int mode) {
        try {
            if (getDisplayWindow == null) {
                getDisplayWindow = method("org.lwjglx.opengl.Display", "getWindow");
            }
            if (glfwSetInputMode == null) {
                glfwSetInputMode = method("org.lwjgl.glfw.GLFW", "glfwSetInputMode",
                        long.class, int.class, int.class);
            }
            if (getDisplayWindow == null || glfwSetInputMode == null) {
                return false;
            }

            long window = ((Number) getDisplayWindow.invoke(null)).longValue();
            if (window == 0L) {
                return false;
            }
            if (mode == GLFW_CURSOR_HIDDEN && systemCursorHidden
                    && systemCursorWindow == window
                    && readSystemCursorMode() == mode) {
                return true;
            }

            glfwSetInputMode.invoke(null, window, GLFW_CURSOR, mode);
            systemCursorWindow = window;
            systemCursorHidden = mode == GLFW_CURSOR_HIDDEN;
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean centerSystemCursor() {
        try {
            if (getDisplayWidth == null) {
                getDisplayWidth = method("org.lwjglx.opengl.Display", "getWidth");
            }
            if (getDisplayHeight == null) {
                getDisplayHeight = method("org.lwjglx.opengl.Display", "getHeight");
            }
            if (glfwGetCursorPos == null) {
                glfwGetCursorPos = method("org.lwjgl.glfw.GLFW", "glfwGetCursorPos",
                        long.class, DoubleBuffer.class, DoubleBuffer.class);
            }
            if (glfwSetCursorPos == null) {
                glfwSetCursorPos = method("org.lwjgl.glfw.GLFW", "glfwSetCursorPos",
                        long.class, double.class, double.class);
            }
            if (getDisplayWindow == null || getDisplayWidth == null || getDisplayHeight == null
                    || glfwGetCursorPos == null || glfwSetCursorPos == null) {
                return false;
            }

            long window = ((Number) getDisplayWindow.invoke(null)).longValue();
            int width = ((Number) getDisplayWidth.invoke(null)).intValue();
            int height = ((Number) getDisplayHeight.invoke(null)).intValue();
            if (window == 0L || width <= 0 || height <= 0) {
                return false;
            }
            double x = width / 2.0;
            double y = height / 2.0;
            cursorXBuffer.clear();
            cursorYBuffer.clear();
            glfwGetCursorPos.invoke(null, window, cursorXBuffer, cursorYBuffer);
            if (cursorXBuffer.get(0) != x || cursorYBuffer.get(0) != y) {
                glfwSetCursorPos.invoke(null, window, x, y);
            }
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static int readSystemCursorMode() {
        try {
            if (getDisplayWindow == null) {
                getDisplayWindow = method("org.lwjglx.opengl.Display", "getWindow");
            }
            if (glfwGetInputMode == null) {
                glfwGetInputMode = method("org.lwjgl.glfw.GLFW", "glfwGetInputMode",
                        long.class, int.class);
            }
            if (getDisplayWindow == null || glfwGetInputMode == null) {
                return -1;
            }
            long window = ((Number) getDisplayWindow.invoke(null)).longValue();
            if (window == 0L) {
                return -1;
            }
            return ((Number) glfwGetInputMode.invoke(null, window, GLFW_CURSOR)).intValue();
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static void logCursorLifecycle(String stage, boolean force) {
        if (!debugLogging) return;

        boolean captured = getBoolean(null, "viewpoint.input.Look", "captured");
        boolean wantCapture = getBoolean(lookWantCapture, "viewpoint.input.Look", "wantCapture");
        int mode = readSystemCursorMode();
        String state = "auto=" + autoCursorRequested
                + ",free=" + isFreeCursor()
                + ",override=" + freeCursorOverrideActive
                + ",captured=" + captured
                + ",wantCapture=" + wantCapture
                + ",mode=" + systemCursorModeName(mode)
                + ",centerPending=" + uiCursorCenterPending;
        if (!force && state.equals(lastCursorLifecycleState)) return;
        lastCursorLifecycleState = state;

        System.out.println("[Viewpoint Extended Support] cursor lifecycle: " + stage
                + ", " + state
                + ", pointer=" + readCursorPositionForLog()
                + ", uiPointer=" + readUiCursorPositionForLog());
    }

    private static void logUiMousePositionSyncFailure(String reason) {
        if (!debugLogging || uiMousePositionSyncFailureLogged) {
            return;
        }
        uiMousePositionSyncFailureLogged = true;
        System.out.println("[Viewpoint Extended Support] UI mouse position sync unavailable: " + reason);
    }

    private static String readCursorPositionForLog() {
        try {
            if (getDisplayWindow == null) {
                getDisplayWindow = method("org.lwjglx.opengl.Display", "getWindow");
            }
            if (glfwGetCursorPos == null) {
                glfwGetCursorPos = method("org.lwjgl.glfw.GLFW", "glfwGetCursorPos",
                        long.class, DoubleBuffer.class, DoubleBuffer.class);
            }
            if (getDisplayWindow == null || glfwGetCursorPos == null) return "unavailable";

            long window = ((Number) getDisplayWindow.invoke(null)).longValue();
            if (window == 0L) return "unavailable";
            cursorXBuffer.clear();
            cursorYBuffer.clear();
            glfwGetCursorPos.invoke(null, window, cursorXBuffer, cursorYBuffer);
            return "(" + cursorXBuffer.get(0) + "," + cursorYBuffer.get(0) + ")";
        } catch (Throwable ignored) {
            return "unavailable";
        }
    }

    private static String readUiCursorPositionForLog() {
        try {
            if (gameMouseGetX == null) {
                gameMouseGetX = method("zombie.input.Mouse", "getXA");
            }
            if (gameMouseGetY == null) {
                gameMouseGetY = method("zombie.input.Mouse", "getYA");
            }
            if (gameMouseGetX == null || gameMouseGetY == null) return "unavailable";

            Object x = gameMouseGetX.invoke(null);
            Object y = gameMouseGetY.invoke(null);
            return "(" + x + "," + y + ")";
        } catch (Throwable ignored) {
            return "unavailable";
        }
    }

    private static String systemCursorModeName(int mode) {
        if (mode == GLFW_CURSOR_NORMAL) return "normal";
        if (mode == GLFW_CURSOR_HIDDEN) return "hidden";
        if (mode == GLFW_CURSOR_DISABLED) return "disabled";
        return Integer.toString(mode);
    }

    private static void recordHook(int hook, String name, boolean original) {
        if (!debugLogging) {
            return;
        }
        long calls;
        switch (hook) {
            case 1 -> calls = ++vanillaCursorHookCalls;
            case 2 -> calls = ++viewpointCursorHookCalls;
            case 3 -> calls = ++directCursorRenderCalls;
            case 4 -> calls = ++vanillaReticleHookCalls;
            case 5 -> calls = ++viewpointReticleHookCalls;
            case 6 -> calls = ++mouseCursorHookCalls;
            case 7 -> calls = ++lookMouseCursorHookCalls;
            case 8 -> calls = ++mouseCursorTextureHookCalls;
            case 9 -> calls = ++mouseCursorVisibilityHookCalls;
            default -> calls = 1L;
        }
        if (calls == 1L) {
            System.out.println("[Viewpoint Extended Support] cursor hook active: "
                    + name + " original=" + original);
            logDiagnosticState();
        }
    }

    private static void logDiagnosticState() {
        if (!debugLogging) {
            return;
        }
        boolean view = isViewEnabled();
        boolean free = isFreeCursor();
        boolean third = isThirdPerson();
        boolean vehicle = third && getCameraVehicle() != null;
        if (diagnosticStateInitialized
                && diagnosticViewEnabled == view
                && diagnosticFreeCursor == free
                && diagnosticAutoCursor == autoCursorRequested
                && diagnosticThirdPerson == third
                && diagnosticVehicle == vehicle) {
            return;
        }
        diagnosticStateInitialized = true;
        diagnosticViewEnabled = view;
        diagnosticFreeCursor = free;
        diagnosticAutoCursor = autoCursorRequested;
        diagnosticThirdPerson = third;
        diagnosticVehicle = vehicle;
        System.out.println("[Viewpoint Extended Support] cursor state: view=" + view
                + ", free=" + free
                + ", auto=" + autoCursorRequested
                + ", third=" + third
                + ", vehicle=" + vehicle
                + ", system=" + systemCursorModeName(readSystemCursorMode()));
    }

    private static boolean getBoolean(Field cached, String className, String fieldName) {
        try {
            Field field = cached;
            if (field == null) {
                field = field(className, fieldName);
                if (fieldName.equals("enabled") && className.equals("viewpoint.core.View")) {
                    viewEnabled = field;
                } else if (fieldName.equals("active") && className.equals("viewpoint.input.ThirdPerson")) {
                    thirdPersonActive = field;
                } else if (fieldName.equals("cursorMode")) {
                    cursorMode = field;
                }
            }
            return field != null && field.getBoolean(null);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void setBoolean(Field cached, String className, String fieldName, boolean value) {
        try {
            Field field = cached == null ? field(className, fieldName) : cached;
            if (field == null) return;
            field.setBoolean(null, value);
            if (fieldName.equals("enabled") && className.equals("viewpoint.core.View")) {
                viewEnabled = field;
            } else if (fieldName.equals("active") && className.equals("viewpoint.input.ThirdPerson")) {
                thirdPersonActive = field;
            } else if (fieldName.equals("cursorMode")) {
                cursorMode = field;
            } else if (fieldName.equals("wantCapture") && className.equals("viewpoint.input.Look")) {
                lookWantCapture = field;
            }
        } catch (Throwable ignored) {
        }
    }

    private static void setFloat(Field cached, String className, String fieldName, float value) {
        try {
            Field field = cached == null ? field(className, fieldName) : cached;
            if (field != null) field.setFloat(null, value);
            if (fieldName.equals("yaw")) lookYaw = field;
            if (fieldName.equals("pitch")) lookPitch = field;
        } catch (Throwable ignored) {
        }
    }

    private static Field field(String className, String fieldName) {
        try {
            Field field = Class.forName(className).getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method method(String className, String methodName, Class<?>... types) {
        try {
            Method method = Class.forName(className).getDeclaredMethod(methodName, types);
            method.setAccessible(true);
            return method;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void call(String className, String methodName) {
        try {
            Method method = methodName.equals("resetCaches") ? resetCaches : null;
            if (method == null) {
                method = method(className, methodName);
                if (className.equals("viewpoint.FP") && methodName.equals("resetCaches")) {
                    resetCaches = method;
                }
            }
            if (method != null) method.invoke(null);
        } catch (Throwable ignored) {
        }
    }

    private static Object callObject(String className, String methodName) {
        try {
            Method method = getCameraCharacter;
            if (method == null) {
                method = method(className, methodName);
                getCameraCharacter = method;
            }
            return method == null ? null : method.invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object getCameraVehicle() {
        Object player = callObject("zombie.iso.IsoCamera", "getCameraCharacter");
        if (player == null) {
            return null;
        }

        try {
            if (vehicleMethodClass != player.getClass()) {
                vehicleMethodClass = player.getClass();
                getPlayerVehicle = player.getClass().getMethod("getVehicle");
                getPlayerVehicle.setAccessible(true);
            }
            return getPlayerVehicle == null ? null : getPlayerVehicle.invoke(player);
        } catch (Throwable ignored) {
            getPlayerVehicle = null;
            return null;
        }
    }

    private static boolean callBoolean(String className, String methodName) {
        Boolean value = callBooleanObject(className, methodName);
        return Boolean.TRUE.equals(value);
    }

    private static Boolean callBooleanObject(String className, String methodName) {
        try {
            Method method = method(className, methodName);
            return method == null ? null : (Boolean) method.invoke(null);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
