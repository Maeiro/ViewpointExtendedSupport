package maeiro.viewpointextendedsupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.zed_0xff.zombie_buddy.Exposer;

@Exposer.LuaClass(name = "ViewpointExtendedSupport")
public final class Bridge {
    private static final int VIEWPOINT_TOGGLE_KEY = 24;
    private static final int INSERT_KEY = 210;
    private static final int LEFT_SHIFT_KEY = 42;
    private static final int RIGHT_SHIFT_KEY = 54;
    private static final int GLFW_CURSOR = 208897;
    private static final int GLFW_CURSOR_NORMAL = 212993;
    private static final int GLFW_CURSOR_HIDDEN = 212994;
    private static final float THIRD_PERSON_ZOOM_STEP = 0.5f;
    private static final float THIRD_PERSON_ZOOM_MIN = -2.0f;
    private static final float THIRD_PERSON_ZOOM_MAX = 8.0f;
    private static final float THIRD_PERSON_BOOM_MIN = 0.5f;

    private static volatile int firstPersonKey = VIEWPOINT_TOGGLE_KEY;
    private static volatile int thirdPersonKey = VIEWPOINT_TOGGLE_KEY;
    private static volatile int viewModeToggleKey;
    private static volatile boolean thirdPersonRequiresShift = true;
    private static volatile boolean holdFreeCursor;
    private static volatile int freeCursorKey;
    private static volatile boolean autoCursorInUi = true;
    private static volatile boolean debugLogging = true;
    private static volatile boolean thirdPersonScrollZoom = true;
    private static volatile float thirdPersonZoomOffset;
    private static volatile int pendingMouseWheel;
    private static volatile boolean autoCursorRequested;
    private static volatile boolean forcedCursor;
    private static volatile boolean lootCursorRequested;
    private static volatile boolean systemCursorHidden;
    private static volatile long systemCursorWindow;

    private static volatile Field viewEnabled;
    private static volatile Field thirdPersonActive;
    private static volatile Field cursorMode;
    private static volatile Field lookYaw;
    private static volatile Field lookPitch;
    private static volatile Method keyPressed;
    private static volatile Method keyDown;
    private static volatile Method eatKeyPress;
    private static volatile Method resetCaches;
    private static volatile Method getCameraCharacter;
    private static volatile Method getPlayerVehicle;
    private static volatile Method getMouseWheelState;
    private static volatile Class<?> vehicleMethodClass;
    private static volatile Method getDisplayWindow;
    private static volatile Method glfwSetInputMode;
    private static volatile Method glfwGetInputMode;

    private static volatile long vanillaCursorHookCalls;
    private static volatile long viewpointCursorHookCalls;
    private static volatile long directCursorRenderCalls;
    private static volatile long vanillaReticleHookCalls;
    private static volatile long viewpointReticleHookCalls;
    private static volatile long mouseCursorHookCalls;
    private static volatile long lookMouseCursorHookCalls;
    private static volatile long mouseCursorTextureHookCalls;
    private static volatile long mouseCursorVisibilityHookCalls;
    private static volatile long directCursorRenderSkippedCalls;
    private static volatile long diagnosticTicks;
    private static volatile boolean diagnosticStateInitialized;
    private static volatile boolean diagnosticViewEnabled;
    private static volatile boolean diagnosticFreeCursor;
    private static volatile boolean diagnosticAutoCursor;
    private static volatile boolean diagnosticThirdPerson;
    private static volatile boolean diagnosticVehicle;

    private Bridge() {
    }

    public static void configure(int firstKey, int thirdKey, boolean requireShift,
                                 boolean holdCursor, int cursorKey, boolean autoUi,
                                 int modeToggleKey, boolean debug) {
        configure(firstKey, thirdKey, requireShift, holdCursor, cursorKey, autoUi,
                modeToggleKey, debug, true);
    }

    public static void configure(int firstKey, int thirdKey, boolean requireShift,
                                 boolean holdCursor, int cursorKey, boolean autoUi,
                                 int modeToggleKey, boolean debug, boolean scrollZoom) {
        firstPersonKey = Math.max(0, firstKey);
        thirdPersonKey = Math.max(0, thirdKey);
        viewModeToggleKey = Math.max(0, modeToggleKey);
        thirdPersonRequiresShift = requireShift;
        holdFreeCursor = holdCursor;
        freeCursorKey = Math.max(0, cursorKey);
        autoCursorInUi = autoUi;
        debugLogging = debug;
        thirdPersonScrollZoom = scrollZoom;
    }

    public static void setAutoCursorRequested(boolean requested) {
        boolean changed = autoCursorRequested != requested;
        autoCursorRequested = requested;
        if (changed) {
            logDiagnosticState();
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
                + ", directCursorSkipped=" + directCursorRenderSkippedCalls
                + ", view=" + isViewEnabled()
                + ", free=" + isFreeCursor()
                + ", auto=" + autoCursorRequested
                + ", third=" + isThirdPerson()
                + ", vehicle=" + isThirdPersonVehicle()
                + ", zoomOffset=" + thirdPersonZoomOffset
                + ", system=" + systemCursorModeName(readSystemCursorMode()));
    }

    public static void pollThirdPersonZoom() {
        int wheel = pendingMouseWheel;
        pendingMouseWheel = 0;
        if (wheel == 0) {
            return;
        }
        if (!canApplyThirdPersonZoom()) {
            return;
        }

        applyThirdPersonZoom(wheel);
    }

    private static boolean canApplyThirdPersonZoom() {
        return thirdPersonScrollZoom && isViewEnabled() && isThirdPerson() && !isFreeCursor();
    }

    private static void applyThirdPersonZoom(int wheel) {
        float previous = thirdPersonZoomOffset;
        float next = clamp(previous - (wheel * THIRD_PERSON_ZOOM_STEP),
                THIRD_PERSON_ZOOM_MIN, THIRD_PERSON_ZOOM_MAX);
        thirdPersonZoomOffset = next;
        if (debugLogging && previous != next) {
            System.out.println("[Viewpoint Extended Support] third-person camera zoom: offset=" + next);
        }
    }

    public static void captureMouseWheel() {
        int wheel = readMouseWheelState();
        if (wheel == 0) {
            return;
        }
        if (debugLogging) {
            System.out.println("[Viewpoint Extended Support] mouse wheel captured: delta=" + wheel);
        }
        if (canApplyThirdPersonZoom()) {
            applyThirdPersonZoom(wheel);
        } else {
            pendingMouseWheel = clampWheel(pendingMouseWheel + wheel);
        }
    }

    public static float adjustThirdPersonBoom(float original) {
        if (!thirdPersonScrollZoom || !isViewEnabled() || !isThirdPerson()) {
            return original;
        }
        return Math.max(THIRD_PERSON_BOOM_MIN, original + thirdPersonZoomOffset);
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

    public static boolean isFreeCursor() {
        return getBoolean(cursorMode, "viewpoint.FP", "cursorMode");
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

    public static boolean overrideMouseCursorUpdate(boolean original) {
        recordHook(6, "Hooks.skipMouseCursorUpdate", original);
        return overrideMouseCursorUpdateInternal(original);
    }

    public static boolean overrideLookMouseCursorUpdate(boolean original) {
        recordHook(7, "Look.onUpdateMouseCursor", original);
        return overrideMouseCursorUpdateInternal(original);
    }

    public static boolean shouldSkipMouseCursorTexture() {
        recordHook(8, "Mouse.renderCursorTexture", true);
        return isViewEnabled();
    }

    public static boolean overrideMouseCursorVisibility(boolean original) {
        recordHook(9, "Mouse.isCursorVisible", original);
        return isViewEnabled() ? false : original;
    }

    private static boolean overrideMouseCursorUpdateInternal(boolean original) {
        if (isViewEnabled() && isFreeCursor()) {
            return setSystemCursorMode(GLFW_CURSOR_HIDDEN);
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

    public static boolean interceptFirstPersonToggle() {
        if (viewModeToggleKey > 0 && firstPersonKey == viewModeToggleKey) {
            return true;
        }
        if (firstPersonKey == VIEWPOINT_TOGGLE_KEY) {
            return false;
        }
        if (firstPersonKey > 0 && isPressed(firstPersonKey)
                && !isDown(INSERT_KEY)) {
            eat(firstPersonKey);
            toggleView();
        }
        return true;
    }

    public static boolean interceptThirdPersonPoll() {
        if (viewModeToggleKey > 0 && isPressed(viewModeToggleKey)
                && !isDown(INSERT_KEY)) {
            eat(viewModeToggleKey);
            toggleViewMode();
            return true;
        }
        if (thirdPersonKey == VIEWPOINT_TOGGLE_KEY) {
            return false;
        }
        if (thirdPersonKey > 0) {
            if (isPressed(thirdPersonKey) && !isDown(INSERT_KEY)) {
                eat(thirdPersonKey);
                setThirdPerson(true);
                System.out.println("[Viewpoint Extended Support] third person "
                        + (isThirdPerson() ? "on" : "off"));
            }
        }
        return true;
    }

    private static void toggleViewMode() {
        if (!isViewEnabled()) {
            toggleView();
            setThirdPerson(false);
            return;
        }

        setThirdPerson(!isThirdPerson());
        System.out.println("[Viewpoint Extended Support] view mode "
                + (isThirdPerson() ? "third person" : "first person"));
    }

    public static void applyCursorOverride() {
        if (!holdFreeCursor && !autoCursorInUi) {
            return;
        }

        boolean lootCursor = lootCursorRequested;
        lootCursorRequested = false;
        boolean settingsCursor = callBoolean("viewpoint.platform.SettingsWindow", "shown");
        boolean paused = callBoolean("zombie.GameTime", "isGamePaused");

        if (holdFreeCursor) {
            boolean held = freeCursorKey > 0 && isDown(freeCursorKey);
            setCursorMode(held || autoCursorRequested || lootCursor || settingsCursor || paused);
            return;
        }

        if (autoCursorRequested) {
            setCursorMode(true);
            forcedCursor = true;
        } else if (forcedCursor && !lootCursor && !settingsCursor && !paused) {
            setCursorMode(false);
            forcedCursor = false;
        }
    }

    private static void toggleView() {
        Object player = callObject("zombie.iso.IsoCamera", "getCameraCharacter");
        if (player == null) {
            return;
        }

        Boolean supported = callBooleanObject("viewpoint.platform.BuildPin", "supported");
        if (Boolean.FALSE.equals(supported)) {
            call("viewpoint.platform.BuildPin", "reportRefused");
            return;
        }

        boolean enabled = !isViewEnabled();
        setViewEnabled(enabled);
        setCursorMode(false);
        call("viewpoint.FP", "resetCaches");
        setLook(player);

        if (enabled) {
            call("viewpoint.Compat", "check");
        }
        System.out.println("[Viewpoint Extended Support] first person "
                + (enabled ? "on" : "off"));
    }

    private static void setLook(Object player) {
        try {
            Method direction = player.getClass().getMethod("getDirectionAngleRadians");
            setFloat(lookYaw, "viewpoint.input.Look", "yaw", ((Number) direction.invoke(player)).floatValue());
            setFloat(lookPitch, "viewpoint.input.Look", "pitch", 0.0f);
        } catch (Throwable ignored) {
        }
    }

    private static boolean isPressed(int key) {
        try {
            if (keyPressed == null) {
                keyPressed = method("zombie.input.GameKeyboard", "isKeyPressed", int.class);
            }
            return keyPressed != null && (Boolean) keyPressed.invoke(null, key);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean isDown(int key) {
        try {
            if (keyDown == null) {
                keyDown = method("zombie.input.GameKeyboard", "isKeyDown", int.class);
            }
            return keyDown != null && (Boolean) keyDown.invoke(null, key);
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static int readMouseWheelState() {
        try {
            if (getMouseWheelState == null) {
                getMouseWheelState = method("zombie.input.Mouse", "getWheelState");
            }
            if (getMouseWheelState == null) {
                return 0;
            }
            return ((Number) getMouseWheelState.invoke(null)).intValue();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static float clamp(float value, float minimum, float maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static int clampWheel(int value) {
        return Math.max(-20, Math.min(20, value));
    }

    private static void eat(int key) {
        try {
            if (eatKeyPress == null) {
                eatKeyPress = method("zombie.input.GameKeyboard", "eatKeyPress", int.class);
            }
            if (eatKeyPress != null) {
                eatKeyPress.invoke(null, key);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void setCursorMode(boolean enabled) {
        boolean previous = isFreeCursor();
        setBoolean(cursorMode, "viewpoint.FP", "cursorMode", enabled);
        if (previous != enabled) {
            logDiagnosticState();
        }
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
                    && systemCursorWindow == window) {
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

    private static String systemCursorModeName(int mode) {
        if (mode == GLFW_CURSOR_NORMAL) return "normal";
        if (mode == GLFW_CURSOR_HIDDEN) return "hidden";
        if (mode == 212995) return "disabled";
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
