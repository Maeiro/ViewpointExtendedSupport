package zombie.input;

import java.util.HashSet;
import java.util.Set;

public final class GameKeyboard {
    public static final Set<Integer> pressed = new HashSet<>();
    public static final Set<Integer> down = new HashSet<>();

    public static boolean isKeyPressed(int key) {
        return pressed.contains(key);
    }

    public static boolean isKeyDown(int key) {
        return down.contains(key);
    }

    public static void eatKeyPress(int key) {
        pressed.remove(key);
    }
}
