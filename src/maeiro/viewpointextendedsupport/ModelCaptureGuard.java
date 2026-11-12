package maeiro.viewpointextendedsupport;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModelCaptureGuard {
    private static Method placed;
    private static Field modelData, modelField, staticModel;
    private static boolean unavailable;
    private static final Set<String> reported = new HashSet<>();

    private ModelCaptureGuard() {
    }

    public static boolean shouldSkip(Object[] arguments) {
        if (unavailable || arguments == null || arguments.length != 10 || arguments[1] == null || arguments[2] == null) return false;
        try {
            Object slot = arguments[1];
            Object character = arguments[2];
            Object pose = arguments[5];
            if (modelData == null) modelData = slot.getClass().getField("modelData");
            List<?> models = (List<?>) modelData.get(slot);
            for (int index = 0; index < models.size(); index++) {
                Object data = models.get(index);
                if (data == null) continue;
                if (modelField == null) modelField = data.getClass().getField("model");
                Object model = modelField.get(data);
                if (model == null) continue;
                if (staticModel == null) staticModel = model.getClass().getField("isStatic");
                if (!staticModel.getBoolean(model)) continue;
                if (placed == null) {
                    Class<?> rates = Class.forName("viewpoint.models.CharacterRate", false, slot.getClass().getClassLoader());
                    for (Method method : rates.getDeclaredMethods()) {
                        Class<?>[] types = method.getParameterTypes();
                        if (method.getName().equals("placed") && types.length == 4 && types[2] == int.class
                                && types[0].isInstance(character) && types[1].isInstance(slot) && types[3].isInstance(pose)) {
                            method.setAccessible(true);
                            placed = method;
                            break;
                        }
                    }
                    if (placed == null) throw new NoSuchMethodException("CharacterRate.placed");
                }
                Object transform;
                try {
                    transform = placed.invoke(null, character, slot, index, pose);
                } catch (InvocationTargetException failure) {
                    if (!(failure.getCause() instanceof NullPointerException)) throw failure;
                    transform = null;
                }
                if (transform == null) {
                    report(data, model);
                    // Capture must stop before scene entries are allocated; returning an invalid draw index is unsafe.
                    return true;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError failure) {
            unavailable = true;
            System.out.println("[Viewpoint Extended Support] Model transform guard unavailable: " + failure);
        }
        return false;
    }

    private static void report(Object data, Object model) {
        if (reported.size() >= 12) return;
        Object instance = optionalField(data, "modelInstance");
        String description = "model=" + optionalField(model, "Name") + ", bone=" + optionalField(instance, "parentBoneName");
        if (reported.add(description)) {
            System.out.println("[Viewpoint Extended Support] Deferred character capture: missing static model transform (" + description + ")");
        }
    }

    private static Object optionalField(Object value, String name) {
        if (value == null) return "unknown";
        try {
            Field field = value.getClass().getField(name);
            Object result = field.get(value);
            return result == null ? "unknown" : result;
        } catch (ReflectiveOperationException failure) {
            return "unknown";
        }
    }
}
