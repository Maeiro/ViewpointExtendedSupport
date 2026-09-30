package zombie.iso;

public final class IsoCamera {
    public static Object character = new Character();

    public static Object getCameraCharacter() {
        return character;
    }

    public static final class Character {
        public Object vehicle;

        public float getDirectionAngleRadians() {
            return 1.25f;
        }

        public Object getVehicle() {
            return vehicle;
        }
    }
}
