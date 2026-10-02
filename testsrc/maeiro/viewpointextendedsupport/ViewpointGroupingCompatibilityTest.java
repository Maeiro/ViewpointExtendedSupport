package maeiro.viewpointextendedsupport;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;

public final class ViewpointGroupingCompatibilityTest {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        Bridge.configure(24, 24, true, false, 56, true, 0, false,
                true, true, 12, 13, true);

        Class<?> rowsClass = Class.forName("viewpoint.interact.LootRows");
        ClassLoader viewpointLoader = rowsClass.getClassLoader();
        Class<?> playerClass = Class.forName("zombie.characters.IsoPlayer", false, viewpointLoader);
        Class<?> targetClass = Class.forName("viewpoint.interact.LootTargets$Target", false, viewpointLoader);
        Class<?> interactionActions = Class.forName("viewpoint.interact.InteractActions", false, viewpointLoader);
        Class<?> lootPanel = Class.forName("viewpoint.interact.LootPanel", false, viewpointLoader);
        interactionActions.getDeclaredMethod("run", playerClass, int.class);
        rowsClass.getDeclaredMethod("clear");
        rowsClass.getDeclaredMethod("build", playerClass, targetClass);
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
        rows.add(action(rowConstructor, nameField, actionField, "Wash: Clothing: Socks", 3));

        Bridge.groupContextMenuActions(rowsObject);

        check(rows.size() == 3, "Viewpoint root menu must contain a category entry");
        check("Wash  >".equals(nameField.get(rows.get(2))), "nested actions should become an activatable category");
        int washGroup = actionField.getInt(rows.get(2));
        selectedField.setInt(rowsObject, 2);
        check(Bridge.handleGroupedContextAction(washGroup), "category selection should enter the nested menu");
        check(rows.size() == 5, "nested menu should contain Back, actions, and a child category");
        check("< Back".equals(nameField.get(rows.get(1))), "nested menu should provide a Back row");
        check("Yourself".equals(nameField.get(rows.get(2))), "child action label should omit the repeated prefix");
        check(actionField.getInt(rows.get(2)) == 1, "child action index must remain unchanged");
        check("Clothing  >".equals(nameField.get(rows.get(4))), "nested labels should support more than one submenu level");
        selectedField.setInt(rowsObject, 4);
        check(Bridge.handleGroupedContextAction(actionField.getInt(rows.get(4))), "child category should open a deeper level");
        check(rows.size() == 3 && "Socks".equals(nameField.get(rows.get(2))), "deeper submenu should show its leaf action");
        check(Bridge.handleGroupedContextAction(actionField.getInt(rows.get(1))), "Back should return one submenu level");
        check(selectedField.getInt(rowsObject) == 4, "Back should restore the selected child category");
        check(Bridge.handleGroupedContextAction(actionField.getInt(rows.get(1))), "Back should return to the root menu");
        check(rows.size() == 3 && selectedField.getInt(rowsObject) == 2, "Back should restore the root list and its selection");
        Bridge.clearGroupedContextMenu(rowsObject);
        check(rows.size() == 5, "clearing Viewpoint rows must restore the unmodified list");
        check("Wash: Yourself".equals(nameField.get(rows.get(2))), "restored menu must retain original labels");

        System.out.println("ViewpointGroupingCompatibilityTest: PASS");
    }

    private static Object action(Constructor<?> constructor, Field name, Field index,
                                 String label, int action) throws Exception {
        Object row = constructor.newInstance();
        name.set(row, label);
        index.setInt(row, action);
        return row;
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
