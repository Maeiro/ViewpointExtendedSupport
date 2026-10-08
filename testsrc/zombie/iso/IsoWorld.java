package zombie.iso;

public final class IsoWorld {
    public static final IsoWorld instance = new IsoWorld();
    public IsoCell currentCell = new IsoCell();

    private IsoWorld() {
    }
}
