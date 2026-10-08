package zombie.Lua;

public final class LuaManager {
    public static Exposer exposer;
    public static se.krka.kahlua.vm.KahluaTable env;

    private LuaManager() {
    }

    public static final class Exposer {
        public void setExposed(Class<?> type) {
        }

        public void exposeLikeJavaRecursively(java.lang.reflect.Type type,
                                              se.krka.kahlua.vm.KahluaTable table) {
        }
    }
}
