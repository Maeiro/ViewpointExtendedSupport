package maeiro.viewpointextendedsupport;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.zed_0xff.zombie_buddy.Exposer;
import zombie.core.physics.BallisticsController;
import zombie.iso.Vector2;
import zombie.iso.Vector3;

@Exposer.LuaClass(name = "ViewpointExtendedSupport")
public final class Bridge {
    private static final int VIEWPOINT_TOGGLE_KEY = 24;
    private static final int INSERT_KEY = 210;
    private static final int LEFT_SHIFT_KEY = 42;
    private static final int RIGHT_SHIFT_KEY = 54;
    private static final int GLFW_CURSOR = 208897;
    private static final int GLFW_CURSOR_NORMAL = 212993;
    private static final int GLFW_CURSOR_HIDDEN = 212994;
    private static final int HIDDEN_RETICLE_COORDINATE = -10000;
    private static final float THIRD_PERSON_ZOOM_STEP = 0.5f;
    private static final float THIRD_PERSON_ZOOM_MIN = -2.0f;
    private static final float THIRD_PERSON_ZOOM_MAX = 8.0f;
    private static final float THIRD_PERSON_BOOM_MIN = 0.5f;
    private static final float VIEWPOINT_VERTICAL_SCALE = 2.4494896f;
    private static final float DEFAULT_FIREARM_RANGE = 8.0f;

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
    private static volatile Method isAimingMethod;
    private static volatile Method setTargetAimPitchMethod;
    private static volatile Method getMuzzlePosition;
    private static volatile Class<?> ballisticsMethodClass;
    private static volatile Field controllerCharacter;
    private static volatile Class<?> controllerClass;
    private static volatile Method getControllerId;
    private static volatile Method getIsoAimingPosition;
    private static volatile Method getCameraTargets;
    private static volatile Method getNumberOfCameraTargets;
    private static volatile Method getCameraTargetsArray;
    private static volatile Field viewpointFrames;
    private static volatile Field frameCamX;
    private static volatile Field frameCamY;
    private static volatile Field frameCamZ;
    private static volatile Field frameEyeX;
    private static volatile Field frameEyeY;
    private static volatile Field frameEyeZ;
    private static volatile Method thirdPersonView;
    private static volatile Field spriteRendererInstance;
    private static volatile Method getMainStateIndex;
    private static volatile Method getAttackingWeapon;
    private static volatile Method getPrimaryHandItem;
    private static volatile Method getMaxRangeWithCharacter;
    private static volatile Method getMaxRangeWithoutCharacter;
    private static volatile Class<?> weaponClass;
    private static volatile Method bulletReticlePosition;
    private static volatile Method bulletReticleQuaternion;
    private static volatile Class<?> bulletClass;
    private static volatile Class<?> jomlVectorClass;
    private static volatile Class<?> jomlQuaternionClass;
    private static volatile Method jomlLookAlong;
    private static volatile Method jomlConjugate;
    private static volatile java.lang.reflect.Constructor<?> jomlVectorConstructor;
    private static volatile java.lang.reflect.Constructor<?> jomlQuaternionConstructor;
    private static volatile Field jomlQuaternionX;
    private static volatile Field jomlQuaternionY;
    private static volatile Field jomlQuaternionZ;
    private static volatile Field jomlQuaternionW;
    private static volatile Object lastBallisticsController;
    private static final Vector3 cameraOrigin = new Vector3();
    private static final Vector3 cameraDirection = new Vector3();
    private static final Vector3 cameraPhysicalDirection = new Vector3();
    private static final Vector3 cameraEndpoint = new Vector3();
    private static final Vector3 cameraTarget = new Vector3();
    private static final Vector3 cameraCandidate = new Vector3();
    private static final Vector3 centeredMuzzle = new Vector3();
    private static final float[] thirdPersonEye = new float[3];
    private static volatile boolean crosshairTarget;
    private static volatile boolean firearmAiming;
    private static volatile int cameraTargetId = -1;
    private static volatile Class<?> firearmCharacterClass;
    private static volatile Method primaryHandItemMethod;
    private static volatile Method firearmAimingMethod;
    private static volatile Class<?> vehicleMethodClass;
    private static volatile Class<?> aimMethodClass;
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
    private static volatile long ballisticsMuzzleHookCalls;
    private static volatile long ballisticsCameraHookCalls;
    private static volatile long ballisticsCameraTargetCount;
    private static volatile boolean ballisticsFailureLogged;
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
        refreshFirearmAiming();
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
                + ", ballisticsMuzzle=" + ballisticsMuzzleHookCalls
                + ", ballisticsCamera=" + ballisticsCameraHookCalls
                + ", ballisticsTargets=" + ballisticsCameraTargetCount
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
        if (!thirdPersonScrollZoom || !isViewEnabled() || !isThirdPerson() || isFreeCursor()) {
            return;
        }

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
        pendingMouseWheel = clampWheel(pendingMouseWheel + wheel);
        if (debugLogging) {
            System.out.println("[Viewpoint Extended Support] mouse wheel captured: delta=" + wheel);
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
        if (!enabled) {
            crosshairTarget = false;
            firearmAiming = false;
            lastBallisticsController = null;
            FirearmTargetOutline.clear();
        }
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
        return shouldSkipReticle(original);
    }

    public static boolean shouldSkipViewpointReticle(boolean original) {
        recordHook(5, "Patch_IsoReticle.enter", original);
        return shouldSkipReticle(original);
    }

    public static boolean shouldSkipViewpointCrosshair() {
        if (!isViewEnabled()) {
            return false;
        }
        if (isFreeCursor() || isThirdPersonVehicle()) {
            return true;
        }
        return firearmAiming && Crosshair.draw(crosshairTarget);
    }

    public static int overrideAimingReticleX(int playerIndex, int original) {
        if (!isViewEnabled() || isFreeCursor() || isThirdPersonVehicle()) {
            return original;
        }
        try {
            return HIDDEN_RETICLE_COORDINATE;
        } catch (Throwable ignored) {
            return original;
        }
    }

    public static int overrideAimingReticleY(int playerIndex, int original) {
        if (!isViewEnabled() || isFreeCursor() || isThirdPersonVehicle()) {
            return original;
        }
        try {
            return HIDDEN_RETICLE_COORDINATE;
        } catch (Throwable ignored) {
            return original;
        }
    }

    public static void adjustViewpointMuzzleDirection(Vector3 direction) {
        if (direction == null || !isViewEnabled() || isFreeCursor() || isThirdPersonVehicle()) {
            return;
        }

        setViewpointAimDirection(direction, direction);
    }

    public static void adjustViewpointMuzzle(Object controller,
                                              Vector3 muzzlePosition,
                                              Vector3 direction) {
        if (controller == null || muzzlePosition == null || direction == null
                || !isViewEnabled() || isFreeCursor() || isThirdPersonVehicle()
                || !isViewpointFirearmController(controller)) {
            return;
        }

        try {
            ballisticsMuzzleHookCalls++;
            if (debugLogging && ballisticsMuzzleHookCalls == 1L) {
                System.out.println("[Viewpoint Extended Support] PZ3D-style ballistics hook active");
            }
            readViewpointCamera(muzzlePosition, cameraOrigin, cameraDirection,
                    cameraPhysicalDirection);

            float range = getFirearmRange(controller);
            float cameraDistance = range + physicalDistance(cameraOrigin, muzzlePosition);
            setEndpoint(cameraOrigin, cameraPhysicalDirection, cameraDistance, cameraEndpoint);
            updateBulletAim(controller, cameraOrigin, cameraPhysicalDirection);
            cameraTarget.set(cameraEndpoint.x, cameraEndpoint.y, cameraEndpoint.z);
            boolean targetFound = selectCameraTarget(controller, cameraDistance, cameraTarget);
            Object character = getControllerCharacter(controller);
            firearmAiming = isCapturedFirearmAim(character);
            crosshairTarget = firearmAiming && targetFound;
            applyPz3dMuzzleCorrection(cameraOrigin, cameraDirection, cameraTarget,
                    muzzlePosition, direction, isThirdPerson());
            setIsoAimingPosition(controller, cameraEndpoint);
            lastBallisticsController = controller;
            if (crosshairTarget) {
                FirearmTargetOutline.update(character, cameraTargetId);
            } else {
                FirearmTargetOutline.clear();
            }
        } catch (Throwable error) {
            reportBallisticsFailure("muzzle", error);
        }
    }

    public static void syncViewpointBallisticsCamera(Object controller) {
        if (controller == null || !isViewEnabled() || isFreeCursor()
                || isThirdPersonVehicle() || !isViewpointFirearmController(controller)) {
            return;
        }

        try {
            ballisticsCameraHookCalls++;
            if (getMuzzlePosition == null || ballisticsMethodClass != controller.getClass()) {
                getMuzzlePosition = publicMethod(controller.getClass(), "getMuzzlePosition");
                ballisticsMethodClass = controller.getClass();
            }
            if (getMuzzlePosition == null) {
                return;
            }

            Vector3 muzzlePosition = (Vector3) getMuzzlePosition.invoke(controller);
            if (muzzlePosition == null) {
                return;
            }

            readViewpointCamera(muzzlePosition, cameraOrigin, cameraDirection,
                    cameraPhysicalDirection);
            updateBulletAim(controller, cameraOrigin, cameraPhysicalDirection);

            if (lastBallisticsController == controller) {
                setIsoAimingPosition(controller, cameraEndpoint);
            }
        } catch (Throwable error) {
            reportBallisticsFailure("camera", error);
        }
    }

    static void applyPz3dMuzzleCorrection(Vector3 cameraOrigin,
                                           Vector3 cameraDirection,
                                           Vector3 endpoint,
                                           Vector3 muzzlePosition,
                                           Vector3 muzzleDirection,
                                           boolean thirdPerson) {
        if (thirdPerson) {
            setDirectionFromTo(muzzlePosition, endpoint, muzzleDirection);
            return;
        }

        centeredOrigin(cameraOrigin, cameraDirection, endpoint, muzzlePosition, centeredMuzzle);
        muzzlePosition.set(centeredMuzzle.x, centeredMuzzle.y, centeredMuzzle.z);
        muzzleDirection.set(cameraDirection.x, cameraDirection.y, cameraDirection.z);
        normalize(muzzleDirection);
    }

    static void centeredOrigin(Vector3 cameraOrigin,
                               Vector3 cameraDirection,
                               Vector3 endpoint,
                               Vector3 muzzlePosition,
                               Vector3 result) {
        float physicalLength = (float) Math.sqrt(cameraDirection.x * cameraDirection.x
                + cameraDirection.y * cameraDirection.y
                + cameraDirection.z * cameraDirection.z
                * VIEWPOINT_VERTICAL_SCALE * VIEWPOINT_VERTICAL_SCALE);
        if (physicalLength <= 0.0001f) {
            result.set(muzzlePosition.x, muzzlePosition.y, muzzlePosition.z);
            return;
        }
        float dx = cameraDirection.x / physicalLength;
        float dy = cameraDirection.y / physicalLength;
        float dz = cameraDirection.z * VIEWPOINT_VERTICAL_SCALE / physicalLength;
        float muzzleAlong = physicalDot(muzzlePosition.x - cameraOrigin.x,
                muzzlePosition.y - cameraOrigin.y,
                (muzzlePosition.z - cameraOrigin.z) * VIEWPOINT_VERTICAL_SCALE,
                dx, dy, dz);
        float endpointAlong = physicalDot(endpoint.x - cameraOrigin.x,
                endpoint.y - cameraOrigin.y,
                (endpoint.z - cameraOrigin.z) * VIEWPOINT_VERTICAL_SCALE,
                dx, dy, dz);
        float along = clamp(muzzleAlong, 0.0f, endpointAlong - 0.01f);
        result.set(cameraOrigin.x + dx * along,
                cameraOrigin.y + dy * along,
                cameraOrigin.z + dz * along / VIEWPOINT_VERTICAL_SCALE);
    }

    private static void setViewpointAimDirection(Vector3 source, Vector3 result) {
        float horizontalLength = (float) Math.sqrt(source.x * source.x + source.y * source.y);
        if (horizontalLength <= 0.0001f) {
            float yaw = getViewpointYaw();
            result.x = (float) Math.cos(yaw);
            result.y = (float) Math.sin(yaw);
            horizontalLength = 1.0f;
        } else if (source != result) {
            result.x = source.x / horizontalLength;
            result.y = source.y / horizontalLength;
        } else {
            result.x /= horizontalLength;
            result.y /= horizontalLength;
        }

        float pitch = getViewpointPitch();
        float horizontalScale = (float) Math.cos(pitch);
        result.x *= horizontalScale;
        result.y *= horizontalScale;
        result.z = (float) Math.sin(pitch);
        normalize(result);
    }

    static void readViewpointCamera(Vector3 fallbackOrigin,
                                    Vector3 originResult,
                                    Vector3 directionResult,
                                    Vector3 physicalDirectionResult) {
        try {
            if (viewpointFrames == null) {
                viewpointFrames = field("viewpoint.FP", "frames");
            }
            if (viewpointFrames == null) {
                setFallbackCamera(fallbackOrigin, originResult, directionResult,
                        physicalDirectionResult);
                return;
            }

            Object frames = viewpointFrames.get(null);
            if (frames == null || !frames.getClass().isArray()
                    || java.lang.reflect.Array.getLength(frames) == 0) {
                setFallbackCamera(fallbackOrigin, originResult, directionResult,
                        physicalDirectionResult);
                return;
            }

            int index = 0;
            Object renderer = getSpriteRendererInstance();
            if (renderer != null) {
                if (getMainStateIndex == null) {
                    getMainStateIndex = publicMethod(renderer.getClass(), "getMainStateIndex");
                }
                if (getMainStateIndex != null) {
                    index = ((Number) getMainStateIndex.invoke(renderer)).intValue();
                }
            }
            index = Math.max(0, Math.min(index, java.lang.reflect.Array.getLength(frames) - 1));
            Object frame = java.lang.reflect.Array.get(frames, index);
            if (frame == null) {
                setFallbackCamera(fallbackOrigin, originResult, directionResult,
                        physicalDirectionResult);
                return;
            }

            if (frameCamX == null) {
                frameCamX = field(frame.getClass().getName(), "camX");
                frameCamY = field(frame.getClass().getName(), "camY");
                frameCamZ = field(frame.getClass().getName(), "camZ");
                frameEyeX = field(frame.getClass().getName(), "eyeX");
                frameEyeY = field(frame.getClass().getName(), "eyeY");
                frameEyeZ = field(frame.getClass().getName(), "eyeZ");
            }
            if (frameCamX == null || frameCamY == null || frameCamZ == null
                    || frameEyeX == null || frameEyeY == null || frameEyeZ == null) {
                setFallbackCamera(fallbackOrigin, originResult, directionResult,
                        physicalDirectionResult);
                return;
            }

            float yaw = getViewpointYaw();
            float pitch = getViewpointPitch();
            originResult.set(frameCamX.getFloat(frame) - frameEyeX.getFloat(frame)
                            + (float) Math.cos(yaw) * 0.12f,
                    frameCamY.getFloat(frame) - frameEyeZ.getFloat(frame)
                            + (float) Math.sin(yaw) * 0.12f,
                    frameCamZ.getFloat(frame) + frameEyeY.getFloat(frame)
                            / VIEWPOINT_VERTICAL_SCALE);
            if (isThirdPerson()) {
                if (thirdPersonView == null) {
                    thirdPersonView = publicMethod(Class.forName("viewpoint.input.ThirdPerson"),
                            "view", frame.getClass(), float.class, float.class, float[].class);
                }
                if (thirdPersonView != null
                        && Boolean.TRUE.equals(thirdPersonView.invoke(null, frame, yaw, pitch,
                                thirdPersonEye))) {
                    originResult.set(frameCamX.getFloat(frame) - thirdPersonEye[0],
                            frameCamY.getFloat(frame) - thirdPersonEye[2],
                            frameCamZ.getFloat(frame)
                                    + thirdPersonEye[1] / VIEWPOINT_VERTICAL_SCALE);
                }
            }
            setViewpointDirection(yaw, pitch,
                    directionResult, physicalDirectionResult);
        } catch (Throwable ignored) {
            setFallbackCamera(fallbackOrigin, originResult, directionResult,
                    physicalDirectionResult);
        }
    }

    private static void setFallbackCamera(Vector3 fallbackOrigin,
                                          Vector3 originResult,
                                          Vector3 directionResult,
                                          Vector3 physicalDirectionResult) {
        originResult.set(fallbackOrigin.x, fallbackOrigin.y, fallbackOrigin.z);
        setViewpointDirection(getViewpointYaw(), getViewpointPitch(),
                directionResult, physicalDirectionResult);
    }

    private static void setViewpointDirection(float yaw, float pitch,
                                              Vector3 directionResult,
                                              Vector3 physicalDirectionResult) {
        float horizontal = (float) Math.cos(pitch);
        physicalDirectionResult.set((float) Math.cos(yaw) * horizontal,
                (float) Math.sin(yaw) * horizontal,
                (float) Math.sin(pitch));
        normalize(physicalDirectionResult);
        directionResult.set(physicalDirectionResult.x,
                physicalDirectionResult.y,
                physicalDirectionResult.z / VIEWPOINT_VERTICAL_SCALE);
        normalize(directionResult);
    }

    private static boolean isViewpointFirearmController(Object controller) {
        try {
            Object character = getControllerCharacter(controller);
            if (character == null) {
                return false;
            }

            Object cameraCharacter = callObject("zombie.iso.IsoCamera", "getCameraCharacter");
            if (cameraCharacter != null && cameraCharacter != character) {
                return false;
            }

            Object weapon = getCharacterWeapon(character);
            if (weapon == null) {
                return false;
            }
            Method ranged = publicMethod(weapon.getClass(), "isRanged");
            return ranged == null || Boolean.TRUE.equals(ranged.invoke(weapon));
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Object getControllerCharacter(Object controller) {
        try {
            if (controllerClass != controller.getClass()) {
                controllerClass = controller.getClass();
                controllerCharacter = field(controller.getClass().getName(), "isoGameCharacter");
                getControllerId = publicMethod(controller.getClass(), "getID");
                getIsoAimingPosition = publicMethod(controller.getClass(), "getIsoAimingPosition");
                getCameraTargets = publicMethod(controller.getClass(), "getCameraTargets",
                        float.class, boolean.class);
                getNumberOfCameraTargets = publicMethod(controller.getClass(),
                        "getNumberOfCameraTargets");
                getCameraTargetsArray = publicMethod(controller.getClass(), "getCameraTargets");
            }
            return controllerCharacter == null ? null : controllerCharacter.get(controller);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object getCharacterWeapon(Object character) {
        try {
            if (getAttackingWeapon == null) {
                getAttackingWeapon = publicMethod(character.getClass(), "getAttackingWeapon");
            }
            Object weapon = getAttackingWeapon == null ? null : getAttackingWeapon.invoke(character);
            if (weapon == null) {
                if (getPrimaryHandItem == null) {
                    getPrimaryHandItem = publicMethod(character.getClass(), "getPrimaryHandItem");
                }
                weapon = getPrimaryHandItem == null ? null : getPrimaryHandItem.invoke(character);
            }
            return weapon;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static float getFirearmRange(Object controller) {
        try {
            Object character = getControllerCharacter(controller);
            Object weapon = character == null ? null : getCharacterWeapon(character);
            if (weapon == null) {
                return DEFAULT_FIREARM_RANGE;
            }

            if (weaponClass != weapon.getClass()) {
                weaponClass = weapon.getClass();
                getMaxRangeWithCharacter = null;
                getMaxRangeWithoutCharacter = null;
                for (Method candidate : weapon.getClass().getMethods()) {
                    if (!candidate.getName().equals("getMaxRange")) {
                        continue;
                    }
                    candidate.setAccessible(true);
                    if (candidate.getParameterCount() == 1) {
                        getMaxRangeWithCharacter = candidate;
                    } else if (candidate.getParameterCount() == 0) {
                        getMaxRangeWithoutCharacter = candidate;
                    }
                }
            }

            Number value = null;
            if (getMaxRangeWithCharacter != null) {
                value = (Number) getMaxRangeWithCharacter.invoke(weapon, character);
            } else if (getMaxRangeWithoutCharacter != null) {
                value = (Number) getMaxRangeWithoutCharacter.invoke(weapon);
            }
            float range = value == null ? DEFAULT_FIREARM_RANGE : value.floatValue();
            return range > 0.0f ? range : DEFAULT_FIREARM_RANGE;
        } catch (Throwable ignored) {
            return DEFAULT_FIREARM_RANGE;
        }
    }

    private static boolean selectCameraTarget(Object controller, float maxDistance, Vector3 endpoint) {
        cameraTargetId = -1;
        try {
            if (getCameraTargets == null || getNumberOfCameraTargets == null
                    || getCameraTargetsArray == null) {
                return false;
            }

            getCameraTargets.invoke(controller, maxDistance, true);
            int count = ((Number) getNumberOfCameraTargets.invoke(controller)).intValue();
            if (count <= 0) {
                return false;
            }

            float[] targets = (float[]) getCameraTargetsArray.invoke(controller);
            if (targets == null || targets.length < 4) {
                return false;
            }

            Vector3 candidate = cameraCandidate;
            candidate.set(targets[1], targets[3], targets[2] / VIEWPOINT_VERTICAL_SCALE);
            float dx = targets[1] - cameraOrigin.x;
            float dy = targets[3] - cameraOrigin.y;
            float dz = targets[2] - cameraOrigin.z * VIEWPOINT_VERTICAL_SCALE;
            float along = physicalDot(dx, dy, dz, cameraPhysicalDirection.x,
                    cameraPhysicalDirection.y, cameraPhysicalDirection.z);
            float offAxisSquared = dx * dx + dy * dy + dz * dz - along * along;
            if (along > 0.0f && along <= maxDistance && offAxisSquared <= 0.1225f) {
                endpoint.set(candidate.x, candidate.y, candidate.z);
                cameraTargetId = (int) targets[0];
                ballisticsCameraTargetCount++;
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    private static void setIsoAimingPosition(Object controller, Vector3 endpoint) {
        try {
            if (getIsoAimingPosition == null) {
                return;
            }
            Object aimingPosition = getIsoAimingPosition.invoke(controller);
            if (aimingPosition instanceof Vector3) {
                ((Vector3) aimingPosition).set(endpoint.x, endpoint.y, endpoint.z);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void updateBulletAim(Object controller, Vector3 origin, Vector3 physicalDirection) {
        try {
            if (getControllerId == null) {
                throw new IllegalStateException("BallisticsController.getID unavailable");
            }
            int id = ((Number) getControllerId.invoke(controller)).intValue();
            if (bulletClass == null) {
                bulletClass = Class.forName("zombie.core.physics.Bullet");
                bulletReticlePosition = publicMethod(bulletClass,
                        "updateBallisticsAimReticlePosition", int.class,
                        float.class, float.class, float.class);
                bulletReticleQuaternion = publicMethod(bulletClass,
                        "updateBallisticsAimReticleQuaternion", int.class,
                        float.class, float.class, float.class, float.class);
            }
            if (bulletReticlePosition == null) {
                throw new IllegalStateException("Bullet reticle position unavailable");
            }
            bulletReticlePosition.invoke(null, id, origin.x,
                    origin.z * VIEWPOINT_VERTICAL_SCALE, origin.y);
            updateBulletQuaternion(id, physicalDirection);
        } catch (Throwable error) {
            reportBallisticsFailure("Bullet reticle", error);
        }
    }

    private static void updateBulletQuaternion(int id, Vector3 direction) {
        try {
            if (bulletReticleQuaternion == null) {
                throw new IllegalStateException("Bullet reticle quaternion unavailable");
            }
            if (jomlVectorClass == null) {
                jomlVectorClass = Class.forName("org.joml.Vector3f");
                jomlQuaternionClass = Class.forName("org.joml.Quaternionf");
                jomlVectorConstructor = jomlVectorClass.getConstructor(
                        float.class, float.class, float.class);
                jomlQuaternionConstructor = jomlQuaternionClass.getConstructor();
                for (Method candidate : jomlQuaternionClass.getMethods()) {
                    if (candidate.getName().equals("lookAlong")
                            && candidate.getParameterCount() == 2) {
                        jomlLookAlong = candidate;
                    } else if (candidate.getName().equals("conjugate")
                            && candidate.getParameterCount() == 0) {
                        jomlConjugate = candidate;
                    }
                }
                jomlQuaternionX = jomlQuaternionClass.getField("x");
                jomlQuaternionY = jomlQuaternionClass.getField("y");
                jomlQuaternionZ = jomlQuaternionClass.getField("z");
                jomlQuaternionW = jomlQuaternionClass.getField("w");
            }
            if (jomlLookAlong == null || jomlConjugate == null) {
                throw new IllegalStateException("JOML camera rotation unavailable");
            }

            Object physicalDirection = jomlVectorConstructor.newInstance(
                    direction.x, direction.z, direction.y);
            float upX = 0.0f;
            float upY = Math.abs(direction.z) < 0.999f ? 1.0f : 0.0f;
            float upZ = Math.abs(direction.z) < 0.999f ? 0.0f : 1.0f;
            Object up = jomlVectorConstructor.newInstance(upX, upY, upZ);
            Object quaternion = jomlQuaternionConstructor.newInstance();
            jomlLookAlong.invoke(quaternion, physicalDirection, up);
            jomlConjugate.invoke(quaternion);
            bulletReticleQuaternion.invoke(null, id,
                    jomlQuaternionX.getFloat(quaternion),
                    jomlQuaternionY.getFloat(quaternion),
                    jomlQuaternionZ.getFloat(quaternion),
                    jomlQuaternionW.getFloat(quaternion));
        } catch (Throwable error) {
            reportBallisticsFailure("Bullet rotation", error);
        }
    }

    private static void reportBallisticsFailure(String stage, Throwable error) {
        if (!debugLogging || ballisticsFailureLogged) {
            return;
        }
        ballisticsFailureLogged = true;
        System.out.println("[Viewpoint Extended Support] ballistics failure at " + stage);
        error.printStackTrace(System.out);
    }

    private static Object getSpriteRendererInstance() {
        try {
            if (spriteRendererInstance == null) {
                spriteRendererInstance = field("zombie.core.SpriteRenderer", "instance");
            }
            return spriteRendererInstance == null ? null : spriteRendererInstance.get(null);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static void setEndpoint(Vector3 origin, Vector3 physicalDirection,
                                    float distance, Vector3 endpoint) {
        endpoint.set(origin.x + physicalDirection.x * distance,
                origin.y + physicalDirection.y * distance,
                origin.z + physicalDirection.z * distance / VIEWPOINT_VERTICAL_SCALE);
    }

    private static void setDirectionFromTo(Vector3 origin, Vector3 endpoint, Vector3 result) {
        result.set(endpoint.x - origin.x, endpoint.y - origin.y, endpoint.z - origin.z);
        normalize(result);
    }

    private static float physicalDistance(Vector3 firstIso, Vector3 secondIso) {
        float x = firstIso.x - secondIso.x;
        float y = firstIso.y - secondIso.y;
        float z = (firstIso.z - secondIso.z) * VIEWPOINT_VERTICAL_SCALE;
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private static float physicalDot(float x, float y, float z,
                                     float directionX, float directionY, float directionZ) {
        return x * directionX + y * directionY + z * directionZ;
    }

    private static void normalize(Vector3 vector) {
        float length = (float) Math.sqrt(vector.x * vector.x
                + vector.y * vector.y + vector.z * vector.z);
        if (length > 0.0001f) {
            vector.x /= length;
            vector.y /= length;
            vector.z /= length;
        }
    }

    public static void syncViewpointAimPitch(Object player) {
        if (player == null || !isViewEnabled() || isFreeCursor() || isThirdPersonVehicle()) {
            return;
        }

        try {
            if (aimMethodClass != player.getClass()
                    || isAimingMethod == null || setTargetAimPitchMethod == null) {
                isAimingMethod = player.getClass().getMethod("isAiming");
                setTargetAimPitchMethod = player.getClass().getMethod(
                        "setTargetVerticalAimAngle", float.class);
                isAimingMethod.setAccessible(true);
                setTargetAimPitchMethod.setAccessible(true);
                aimMethodClass = player.getClass();
            }
            if (!Boolean.TRUE.equals(isAimingMethod.invoke(player))) {
                return;
            }
            setTargetAimPitchMethod.invoke(player,
                    (float) Math.toDegrees(getViewpointPitch()));
        } catch (Throwable ignored) {
        }
    }

    public static void overrideCalculatedAimVector(Object player, Vector2 result) {
        if (result == null || !isCapturedFirearmAim(player)) {
            return;
        }
        float yaw = getViewpointYaw();
        result.set((float) Math.cos(yaw), (float) Math.sin(yaw));
    }

    public static void stabilizeAimVector(Object controller,
                                          BallisticsController.AimingVectorParameters parameters,
                                          boolean valid) {
        if (!valid || parameters == null
                || !isCapturedFirearmAim(getControllerCharacter(controller))) {
            return;
        }

        float yaw = getViewpointYaw();
        float pitch = getViewpointPitch();
        float horizontal = (float) Math.cos(pitch);
        float x = (float) Math.cos(yaw);
        float y = (float) Math.sin(yaw);
        parameters.desiredForward.set(x * horizontal, y * horizontal,
                (float) Math.sin(pitch) / VIEWPOINT_VERTICAL_SCALE);
        parameters.desiredForward.normalize();
        parameters.desiredForward2f.set(x, y);
        parameters.desiredForwardPitchRads = pitch;
    }

    private static void refreshFirearmAiming() {
        firearmAiming = isCapturedFirearmAim(
                callObject("zombie.iso.IsoCamera", "getCameraCharacter"));
        if (!firearmAiming) {
            crosshairTarget = false;
            FirearmTargetOutline.clear();
        }
    }

    private static boolean isCapturedFirearmAim(Object character) {
        if (character == null || !isViewEnabled() || isFreeCursor() || isThirdPersonVehicle()) {
            return false;
        }
        Object cameraCharacter = callObject("zombie.iso.IsoCamera", "getCameraCharacter");
        if (cameraCharacter != character) {
            return false;
        }
        try {
            if (firearmCharacterClass != character.getClass()) {
                firearmCharacterClass = character.getClass();
                firearmAimingMethod = publicMethod(firearmCharacterClass, "isAiming");
                primaryHandItemMethod = publicMethod(firearmCharacterClass, "getPrimaryHandItem");
            }
            if (firearmAimingMethod == null || primaryHandItemMethod == null
                    || !Boolean.TRUE.equals(firearmAimingMethod.invoke(character))) {
                return false;
            }
            Object weapon = primaryHandItemMethod.invoke(character);
            Method ranged = weapon == null ? null : publicMethod(weapon.getClass(), "isRanged");
            return ranged != null && Boolean.TRUE.equals(ranged.invoke(weapon));
        } catch (Throwable ignored) {
            return false;
        }
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

    private static boolean shouldSkipReticle(boolean original) {
        if (!isViewEnabled()) {
            return original;
        }
        if (isFreeCursor() || isThirdPersonVehicle()) {
            return true;
        }

        return false;
    }

    private static float getViewpointYaw() {
        try {
            if (lookYaw == null) {
                lookYaw = field("viewpoint.input.Look", "yaw");
            }
            return lookYaw == null ? 0.0f : lookYaw.getFloat(null);
        } catch (Throwable ignored) {
            return 0.0f;
        }
    }

    private static float getViewpointPitch() {
        try {
            if (lookPitch == null) {
                lookPitch = field("viewpoint.input.Look", "pitch");
            }
            return lookPitch == null ? 0.0f : lookPitch.getFloat(null);
        } catch (Throwable ignored) {
            return 0.0f;
        }
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
            autoCursorRequested = false;
            lootCursorRequested = false;
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

    private static Method publicMethod(Class<?> type, String methodName, Class<?>... types) {
        try {
            Method method = type.getMethod(methodName, types);
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
