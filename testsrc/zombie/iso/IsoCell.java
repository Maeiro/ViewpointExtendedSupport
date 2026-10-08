package zombie.iso;

import java.util.HashMap;
import java.util.Map;

public final class IsoCell {
    private final Map<String, IsoGridSquare> squares = new HashMap<>();

    public void addSquare(IsoGridSquare square) {
        squares.put(key(square.x, square.y, square.z), square);
    }

    public IsoGridSquare getGridSquare(int x, int y, int z) {
        return squares.get(key(x, y, z));
    }

    private static String key(int x, int y, int z) {
        return x + "," + y + "," + z;
    }
}
