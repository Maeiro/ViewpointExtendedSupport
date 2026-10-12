package maeiro.viewpointextendedsupport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import me.zed_0xff.zombie_buddy.Patch;

public final class ViewpointGroupingCompatibilityTest {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Bridge.configure(false, 56, true, false, true, true, 0);

        Class<?> rowsClass = Class.forName("viewpoint.interact.LootRows");
        ClassLoader viewpointLoader = rowsClass.getClassLoader();
        Class<?> playerClass = Class.forName("zombie.characters.IsoPlayer", false, viewpointLoader);
        Class<?> targetClass = Class.forName("viewpoint.interact.LootTargets$Target", false, viewpointLoader);
        Class<?> interactionActions = Class.forName("viewpoint.interact.InteractActions", false, viewpointLoader);
        Class<?> lootPanel = Class.forName("viewpoint.interact.LootPanel", false, viewpointLoader);
        interactionActions.getDeclaredMethod("run", playerClass, int.class);
        rowsClass.getDeclaredMethod("clear");
        rowsClass.getDeclaredMethod("build", playerClass, targetClass);
        Patch hook = Patches.ViewpointInteractionGroups.class.getAnnotation(Patch.class);
        check(hook != null && "build".equals(hook.methodName()),
                "container cascade should run after Viewpoint has assembled the full interaction menu");
        check(Patches.ViewpointInteractionGroups.class.getDeclaredMethod("exit", Object.class, Object.class) != null,
                "the grouping hook must receive the selected target");
        Constructor<?> rowsConstructor = rowsClass.getDeclaredConstructor();
        rowsConstructor.setAccessible(true);
        Object rowsObject = rowsConstructor.newInstance();
        Field rowsField = rowsClass.getDeclaredField("rows");
        rowsField.setAccessible(true);
        ArrayList<Object> rows = (ArrayList<Object>) rowsField.get(rowsObject);

        Class<?> rowClass = Class.forName("viewpoint.interact.LootRows$Row");
        Constructor<?> rowConstructor = rowClass.getDeclaredConstructor();
        rowConstructor.setAccessible(true);
        Field headingField = rowClass.getDeclaredField("heading");
        Field nameField = rowClass.getDeclaredField("name");
        Field actionField = rowClass.getDeclaredField("action");
        rowClass.getDeclaredField("icon");
        lootPanel.getDeclaredMethod("besides", Class.forName("zombie.ui.TextManager", false, viewpointLoader),
                rowClass, int.class);
        Field selectedField = rowsClass.getDeclaredField("selected");
        headingField.setAccessible(true);
        nameField.setAccessible(true);
        actionField.setAccessible(true);
        selectedField.setAccessible(true);

        Object menuHeading = rowConstructor.newInstance();
        headingField.set(menuHeading, "White Toilet");
        rows.add(menuHeading);
        rows.add(action(rowConstructor, nameField, actionField, "Drink", 0));
        rows.add(action(rowConstructor, nameField, actionField, "Wash: Yourself", 1));
        rows.add(action(rowConstructor, nameField, actionField, "Wash: All Clothing", 2));
        Object socksAction = action(rowConstructor, nameField, actionField, "Wash: Clothing: Socks", 3);
        rows.add(socksAction);

        Bridge.groupContextMenuActions(rowsObject);

        check(rows.size() == 3, "Viewpoint root menu must contain a category entry");
        check("Wash: Clothing: Socks".equals(nameField.get(socksAction)),
                "actions in unopened submenus must not be rewritten eagerly");
        check("Wash  >".equals(nameField.get(rows.get(2))), "nested actions should become an activatable category");
        int washGroup = actionField.getInt(rows.get(2));
        selectedField.setInt(rowsObject, 2);
        check(Bridge.handleGroupedContextAction(washGroup), "category selection should enter the nested menu");
        check(rows.size() == 5, "nested menu should contain Back, actions, and a child category");
        check("Wash: Clothing: Socks".equals(nameField.get(socksAction)),
                "deeper submenu actions must remain untouched until their own category is opened");
        check("< Back".equals(nameField.get(rows.get(1))), "nested menu should provide a Back row");
        check("Yourself".equals(nameField.get(rows.get(2))), "child action label should omit the repeated prefix");
        check(actionField.getInt(rows.get(2)) == 1, "child action index must remain unchanged");
        check("Clothing  >".equals(nameField.get(rows.get(4))), "nested labels should support more than one submenu level");
        selectedField.setInt(rowsObject, 4);
        check(Bridge.handleGroupedContextAction(actionField.getInt(rows.get(4))), "child category should open a deeper level");
        check(rows.size() == 3 && "Socks".equals(nameField.get(rows.get(2))), "deeper submenu should show its leaf action");
        check("Socks".equals(nameField.get(socksAction)), "opening a submenu should shorten only its visible leaf labels");
        check(Bridge.handleGroupedContextAction(actionField.getInt(rows.get(1))), "Back should return one submenu level");
        check(selectedField.getInt(rowsObject) == 4, "Back should restore the selected child category");
        check(Bridge.handleGroupedContextAction(actionField.getInt(rows.get(1))), "Back should return to the root menu");
        check(rows.size() == 3 && selectedField.getInt(rowsObject) == 2, "Back should restore the root list and its selection");
        check(Bridge.handleGroupedContextAction(actionField.getInt(rows.get(2))),
                "a previously opened submenu should be reusable");
        check(rows.size() == 5 && "Clothing  >".equals(nameField.get(rows.get(4))),
                "reopening a submenu must not duplicate or lose its children");
        Bridge.clearGroupedContextMenu(rowsObject);
        check(rows.size() == 5, "clearing Viewpoint rows must restore the unmodified list");
        check("Wash: Yourself".equals(nameField.get(rows.get(2))), "restored menu must retain original labels");

        Object containerRowsObject = rowsConstructor.newInstance();
        ArrayList<Object> containerRows = (ArrayList<Object>) rowsField.get(containerRowsObject);
        Field itemsField = rowClass.getDeclaredField("items");
        itemsField.setAccessible(true);
        Object gloveboxItem = new Object();
        Object trunkItem = new Object();
        addContainer(containerRows, rowConstructor, headingField, nameField, actionField, itemsField,
                "Glovebox", "Pistol", gloveboxItem);
        addContainer(containerRows, rowConstructor, headingField, nameField, actionField, itemsField,
                "Trunk", "Water Bottle", trunkItem);
        Object interactionHeading = rowConstructor.newInstance();
        headingField.set(interactionHeading, "Interaction");
        containerRows.add(interactionHeading);
        containerRows.add(action(rowConstructor, nameField, actionField, "Open Door", 10));
        Constructor<?> targetConstructor = targetClass.getDeclaredConstructor();
        targetConstructor.setAccessible(true);
        Object nonGroundTarget = targetConstructor.newInstance();
        Field targetKindField = targetClass.getDeclaredField("kind");
        targetKindField.setAccessible(true);
        targetKindField.setInt(nonGroundTarget, 1);
        Bridge.groupContextMenuActions(containerRowsObject, nonGroundTarget);

        check(containerRows.size() == 4, "container contents should collapse into one root entry per container");
        check("Glovebox (1)  >".equals(nameField.get(containerRows.get(0))),
                "the container submenu should show its name and item count");
        check("Trunk (1)  >".equals(nameField.get(containerRows.get(1))),
                "multiple containers should remain separate root entries");
        check("Interaction".equals(headingField.get(containerRows.get(2))),
                "container entries should appear before the interaction actions section");
        int gloveboxGroup = actionField.getInt(containerRows.get(0));
        check(Bridge.handleGroupedContextAction(gloveboxGroup), "selecting a container should open its contents");
        check(containerRows.size() == 2 && "< Back".equals(nameField.get(containerRows.get(0))),
                "opening a container should hide unrelated interaction rows");
        check("Pistol".equals(nameField.get(containerRows.get(1))),
                "opening a container should reveal its original item rows");
        check(actionField.getInt(containerRows.get(1)) == -1
                        && !((ArrayList<?>) itemsField.get(containerRows.get(1))).isEmpty(),
                "container item rows must remain native loot rows selectable by Viewpoint");
        Bridge.clearGroupedContextMenu(containerRowsObject);
        check(containerRows.size() == 6 && "Glovebox".equals(headingField.get(containerRows.get(0)))
                        && "Pistol".equals(nameField.get(containerRows.get(1)))
                        && "Trunk".equals(headingField.get(containerRows.get(2)))
                        && "Interaction".equals(headingField.get(containerRows.get(4))),
                "closing the menu must restore the original order and all container rows");

        Bridge.configureContainerContentsCascade(false);
        Object flatRowsObject = rowsConstructor.newInstance();
        ArrayList<Object> flatRows = (ArrayList<Object>) rowsField.get(flatRowsObject);
        addContainer(flatRows, rowConstructor, headingField, nameField, actionField, itemsField,
                "Backpack", "Bandage", new Object());
        flatRows.add(action(rowConstructor, nameField, actionField, "Open Door", 11));
        Bridge.groupContextMenuActions(flatRowsObject, nonGroundTarget);
        check(flatRows.size() == 3 && "Backpack".equals(headingField.get(flatRows.get(0)))
                        && "Bandage".equals(nameField.get(flatRows.get(1))),
                "disabling container cascade should leave native container rows visible");
        Bridge.configureContainerContentsCascade(true);

        Bridge.configure(false, 56, true, false, true, false, 0);
        Object independentRowsObject = rowsConstructor.newInstance();
        ArrayList<Object> independentRows = (ArrayList<Object>) rowsField.get(independentRowsObject);
        addContainer(independentRows, rowConstructor, headingField, nameField, actionField, itemsField,
                "Satchel", "Matches", new Object());
        Object actionHeading = rowConstructor.newInstance();
        headingField.set(actionHeading, "Interaction");
        independentRows.add(actionHeading);
        independentRows.add(action(rowConstructor, nameField, actionField, "Drink: Water", 12));
        Bridge.groupContextMenuActions(independentRowsObject, nonGroundTarget);
        check("Satchel (1)  >".equals(nameField.get(independentRows.get(0)))
                        && "Drink: Water".equals(nameField.get(independentRows.get(2))),
                "container cascading should work independently of grouped action menus");
        Bridge.clearGroupedContextMenu(independentRowsObject);
        Bridge.configure(false, 56, true, false, true, true, 0);

        System.out.println("ViewpointGroupingCompatibilityTest: PASS");
    }

    private static Object action(Constructor<?> constructor, Field name, Field index,
                                 String label, int action) throws Exception {
        Object row = constructor.newInstance();
        name.set(row, label);
        index.setInt(row, action);
        return row;
    }

    @SuppressWarnings("unchecked")
    private static void addContainer(ArrayList<Object> rows, Constructor<?> constructor,
                                    Field heading, Field name, Field action, Field items,
                                    String container, String itemName, Object item) throws Exception {
        Object headingRow = constructor.newInstance();
        heading.set(headingRow, container);
        rows.add(headingRow);
        Object itemRow = constructor.newInstance();
        name.set(itemRow, itemName);
        ((ArrayList<Object>) items.get(itemRow)).add(item);
        rows.add(itemRow);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
