package maeiro.viewpointextendedsupport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import me.zed_0xff.zombie_buddy.Exposer;

@Exposer.LuaClass(name = "ViewpointExtendedSupport")
public final class Bridge {
    private static final int VIEWPOINT_TOGGLE_KEY = 24;
    private static final int INSERT_KEY = 210;
    private static final int LEFT_SHIFT_KEY = 42;
    private static final int RIGHT_SHIFT_KEY = 54;
    private static final int DEFAULT_ZOOM_OUT_KEY = 12;
    private static final int DEFAULT_ZOOM_IN_KEY = 13;
    private static final int GROUP_ACTION_START = 1_000_000_000;
    private static final int GROUP_BACK_ACTION = Integer.MAX_VALUE;
    private static final Map<String, Object> interactionIcons = new HashMap<>();
    private static final Map<String, Object> vanillaInteractionIcons = new HashMap<>();
    private static final int GLFW_CURSOR = 208897;
    private static final int GLFW_CURSOR_NORMAL = 212993;
    private static final int GLFW_CURSOR_HIDDEN = 212994;
    private static final int GLFW_CURSOR_DISABLED = 212995;
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
    private static volatile int thirdPersonZoomOutKey = DEFAULT_ZOOM_OUT_KEY;
    private static volatile int thirdPersonZoomInKey = DEFAULT_ZOOM_IN_KEY;
    private static volatile boolean groupContextMenuActionsEnabled = true;
    private static volatile boolean skipSetupWizard = true;
    private static volatile float thirdPersonZoomOffset;
    private static volatile int pendingMouseWheel;
    private static volatile boolean autoCursorRequested;
    private static volatile boolean forcedCursor;
    private static volatile boolean freeCursorOverrideActive;
    private static volatile boolean lootCursorRequested;
    private static volatile boolean systemCursorHidden;
    private static volatile long systemCursorWindow;

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
    private static volatile InteractionMenu activeInteractionMenu;
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

    private Bridge() {
    }

    public static void configure(int firstKey, int thirdKey, boolean requireShift,
                                 boolean holdCursor, int cursorKey, boolean autoUi,
                                 int modeToggleKey, boolean debug) {
        configure(firstKey, thirdKey, requireShift, holdCursor, cursorKey, autoUi,
                modeToggleKey, debug, true, true);
    }

    public static void configure(int firstKey, int thirdKey, boolean requireShift,
                                 boolean holdCursor, int cursorKey, boolean autoUi,
                                 int modeToggleKey, boolean debug, boolean scrollZoom) {
        configure(firstKey, thirdKey, requireShift, holdCursor, cursorKey, autoUi,
                modeToggleKey, debug, scrollZoom, true);
    }

    public static void configure(int firstKey, int thirdKey, boolean requireShift,
                                 boolean holdCursor, int cursorKey, boolean autoUi,
                                 int modeToggleKey, boolean debug, boolean scrollZoom,
                                 boolean skipWizard) {
        configure(firstKey, thirdKey, requireShift, holdCursor, cursorKey, autoUi,
                modeToggleKey, debug, scrollZoom, skipWizard,
                DEFAULT_ZOOM_OUT_KEY, DEFAULT_ZOOM_IN_KEY);
    }

    public static void configure(int firstKey, int thirdKey, boolean requireShift,
                                 boolean holdCursor, int cursorKey, boolean autoUi,
                                 int modeToggleKey, boolean debug, boolean scrollZoom,
                                 boolean skipWizard, int zoomOutKey, int zoomInKey) {
        configure(firstKey, thirdKey, requireShift, holdCursor, cursorKey, autoUi,
                modeToggleKey, debug, scrollZoom, skipWizard, zoomOutKey, zoomInKey, true);
    }

    public static void configure(int firstKey, int thirdKey, boolean requireShift,
                                 boolean holdCursor, int cursorKey, boolean autoUi,
                                 int modeToggleKey, boolean debug, boolean scrollZoom,
                                 boolean skipWizard, int zoomOutKey, int zoomInKey,
                                 boolean groupActions) {
        firstPersonKey = Math.max(0, firstKey);
        thirdPersonKey = Math.max(0, thirdKey);
        viewModeToggleKey = Math.max(0, modeToggleKey);
        thirdPersonRequiresShift = requireShift;
        holdFreeCursor = holdCursor;
        freeCursorKey = Math.max(0, cursorKey);
        autoCursorInUi = autoUi;
        debugLogging = debug;
        thirdPersonScrollZoom = scrollZoom;
        skipSetupWizard = skipWizard;
        thirdPersonZoomOutKey = Math.max(0, zoomOutKey);
        thirdPersonZoomInKey = Math.max(0, zoomInKey);
        if (groupContextMenuActionsEnabled != groupActions) {
            groupContextMenuActionsEnabled = groupActions;
            InteractionMenu menu = activeInteractionMenu;
            if (menu != null) {
                if (groupActions) {
                    groupContextMenuActions(menu.rowsObject);
                } else {
                    restoreInteractionMenu(menu);
                    menu.grouped = false;
                    if (applyInteractionIcons(menu.rootEntries, menu.iconField, null)) {
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
        applyInteractionIcons(menu.rootEntries, menu.iconField, null);
        if (menu.grouped) {
            updateVisibleGroupIcons(menu);
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
        boolean changed = autoCursorRequested != requested;
        autoCursorRequested = requested;
        if (changed) {
            logDiagnosticState();
            applyCursorOverride();
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
                + ", zoomOffset=" + thirdPersonZoomOffset
                + ", system=" + systemCursorModeName(readSystemCursorMode()));
    }

    public static void pollThirdPersonZoom() {
        int wheel = pendingMouseWheel;
        pendingMouseWheel = 0;
        if (!isViewEnabled() || !isThirdPerson() || isFreeCursor()) {
            return;
        }

        int steps = thirdPersonScrollZoom ? -wheel : 0;
        if (thirdPersonZoomOutKey > 0 && isPressed(thirdPersonZoomOutKey)) {
            eat(thirdPersonZoomOutKey);
            steps++;
        }
        if (thirdPersonZoomInKey > 0 && isPressed(thirdPersonZoomInKey)) {
            eat(thirdPersonZoomInKey);
            steps--;
        }
        if (steps == 0) {
            return;
        }

        float previous = thirdPersonZoomOffset;
        float next = clamp(previous + (steps * THIRD_PERSON_ZOOM_STEP),
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
        if (!isViewEnabled() || !isThirdPerson()) {
            return original;
        }
        return Math.max(THIRD_PERSON_BOOM_MIN, original + thirdPersonZoomOffset);
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
                menu.actionRows.add(row);
                addInteractionAction(menu, row, name);
            }

            menu.iconField = iconField;
            menu.grouped = groupContextMenuActionsEnabled && containsGroup(menu.rootEntries);
            if (menu.grouped) {
                assignGroupActions(menu, menu.rootEntries);
                setLeafLabels(menu.rootEntries, nameField);
            }

            boolean hasIcons = applyInteractionIcons(menu.rootEntries, iconField, null);
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

    private static void addInteractionAction(InteractionMenu menu, Object row, String label) {
        String[] parts = label == null ? new String[0] : label.split(": ");
        if (parts.length < 2) {
            menu.rootEntries.add(InteractionEntry.action(label, row, label));
            return;
        }

        ArrayList<InteractionEntry> entries = menu.rootEntries;
        for (int index = 0; index < parts.length - 1; index++) {
            InteractionEntry group = findOrCreateGroup(entries, parts[index]);
            entries = group.children;
        }
        String actionName = parts[parts.length - 1];
        entries.add(InteractionEntry.action(actionName, row, label));
    }

    private static InteractionEntry findOrCreateGroup(ArrayList<InteractionEntry> entries, String name) {
        for (InteractionEntry entry : entries) {
            if (entry.group && entry.name.equals(name)) {
                return entry;
            }
        }
        InteractionEntry group = InteractionEntry.group(name);
        entries.add(group);
        return group;
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
            if (entry.group) {
                entry.action = GROUP_ACTION_START + menu.groupsByAction.size();
                menu.groupsByAction.put(entry.action, entry);
                assignGroupActions(menu, entry.children);
            }
        }
    }

    private static void showInteractionLevel(InteractionMenu menu, boolean selectFirst) throws Exception {
        menu.rows.clear();
        menu.rows.addAll(menu.prefixRows);
        ArrayList<InteractionEntry> visibleEntries = menu.path.isEmpty()
                ? menu.rootEntries : menu.path.get(menu.path.size() - 1).children;

        int firstSelectable = -1;
        int backIndex = -1;
        if (!menu.path.isEmpty()) {
            backIndex = menu.rows.size();
            menu.rows.add(createInteractionRow(menu, "< Back", GROUP_BACK_ACTION, null));
        }
        for (InteractionEntry entry : visibleEntries) {
            Object row = entry.group
                    ? createInteractionRow(menu, entry.name + "  >", entry.action, entry.icon)
                    : entry.actionRow;
            menu.rows.add(row);
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
            restoreActionNames(menu.rootEntries, menu.nameField);
            restoreActionIcons(menu.rootEntries, menu.iconField);
            menu.rows.clear();
            menu.rows.addAll(menu.prefixRows);
            menu.rows.addAll(menu.actionRows);
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

    private static void restoreActionNames(ArrayList<InteractionEntry> entries, Field nameField) throws IllegalAccessException {
        for (InteractionEntry entry : entries) {
            if (entry.group) {
                restoreActionNames(entry.children, nameField);
            } else {
                nameField.set(entry.actionRow, entry.originalName);
            }
        }
    }

    private static void setLeafLabels(ArrayList<InteractionEntry> entries, Field nameField) throws IllegalAccessException {
        for (InteractionEntry entry : entries) {
            if (entry.group) {
                setLeafLabels(entry.children, nameField);
            } else if (entry.name == null ? entry.originalName != null : !entry.name.equals(entry.originalName)) {
                nameField.set(entry.actionRow, entry.name);
            }
        }
    }

    private static boolean applyInteractionIcons(ArrayList<InteractionEntry> entries, Field iconField,
                                                 Object parentIcon) {
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
                    found |= applyInteractionIcons(entry.children, iconField, entry.icon);
                    continue;
                }

                if (!entry.originalIconCaptured) {
                    entry.originalIcon = iconField.get(entry.actionRow);
                    entry.originalIconCaptured = true;
                }
                Object icon = vanillaContextMenuIcon(entry.originalName);
                if (icon == null) {
                    icon = vanillaContextMenuIcon(entry.name);
                }
                if (icon == null) {
                    icon = contextMenuIcon(entry.originalName);
                }
                if (icon == null) {
                    icon = contextMenuIcon(entry.name);
                }
                if (icon == null) {
                    icon = parentIcon;
                }
                if (icon != null && iconField.getType().isInstance(icon)) {
                    iconField.set(entry.actionRow, icon);
                    found = true;
                } else {
                    iconField.set(entry.actionRow, entry.originalIcon);
                }
            }
        } catch (Throwable failure) {
            logContextGroupingFailure(failure);
        }
        return found;
    }

    private static void restoreActionIcons(ArrayList<InteractionEntry> entries, Field iconField) throws IllegalAccessException {
        for (InteractionEntry entry : entries) {
            if (entry.group) {
                restoreActionIcons(entry.children, iconField);
            } else if (entry.originalIconCaptured) {
                iconField.set(entry.actionRow, entry.originalIcon);
            }
        }
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

    private static final class InteractionMenu {
        private final Object rowsObject;
        private final ArrayList<Object> rows;
        private final ArrayList<Object> prefixRows = new ArrayList<>();
        private final ArrayList<Object> actionRows = new ArrayList<>();
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
        private final Object actionRow;
        private final String originalName;
        private final ArrayList<InteractionEntry> children = new ArrayList<>();
        private Object icon;
        private Object originalIcon;
        private boolean originalIconCaptured;
        private int action;

        private InteractionEntry(String name, boolean group, Object actionRow, String originalName) {
            this.name = name;
            this.group = group;
            this.actionRow = actionRow;
            this.originalName = originalName;
        }

        private static InteractionEntry group(String name) {
            return new InteractionEntry(name, true, null, null);
        }

        private static InteractionEntry action(String name, Object row, String originalName) {
            return new InteractionEntry(name, false, row, originalName);
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
        if (isViewEnabled() && freeCursorOverrideActive) {
            setBoolean(lookWantCapture, "viewpoint.input.Look", "wantCapture", false);
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
        return overrideMouseCursorUpdateInternal(original);
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
        boolean visible = !vehicleCamera && (freeCursor || (!mouseCaptured && original));
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
            boolean held = freeCursorKey > 0 && isDown(freeCursorKey);
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
        System.out.println("[Viewpoint Extended Support] first person "
                + (enabled ? "on" : "off"));
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
