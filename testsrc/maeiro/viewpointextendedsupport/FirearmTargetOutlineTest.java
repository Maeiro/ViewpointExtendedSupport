package maeiro.viewpointextendedsupport;

import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.network.fields.hit.HitInfo;

public final class FirearmTargetOutlineTest {
    public static void main(String[] args) {
        IsoPlayer player = new IsoPlayer(1);
        IsoZombie aimed = new IsoZombie(10);
        IsoZombie nearby = new IsoZombie(11);
        player.getHitInfoList().add(new HitInfo(aimed));
        player.getCell().getZombieList().add(nearby);

        FirearmTargetOutline.update(player, aimed.getID());
        check(aimed.isOutlineHighlight(0) && !nearby.isOutlineHighlight(0),
                "only the zombie under the camera ray should receive an outline");
        aimed.setOutlineHighlight(0, false);
        FirearmTargetOutline.update(player, aimed.getID());
        check(aimed.isOutlineHighlight(0),
                "Viewpoint's per-frame outline reset must not lose the selected target");

        FirearmTargetOutline.update(player, nearby.getID());
        check(!aimed.isOutlineHighlight(0) && nearby.isOutlineHighlight(0),
                "switching targets should transfer the outline without leaving a stale one");

        FirearmTargetOutline.update(player, -1);
        check(!nearby.isOutlineHighlight(0) && nearby.getOutlineHighlightCol(0) == 0,
                "losing the target should clear our outline and restore its color");

        FirearmTargetOutline.update(player, nearby.getID());
        nearby.setOutlineHighlightCol(0, 0.0f, 1.0f, 0.0f, 1.0f);
        int replacementColor = nearby.getOutlineHighlightCol(0);
        FirearmTargetOutline.clear();
        check(nearby.isOutlineHighlight(0)
                        && nearby.getOutlineHighlightCol(0) == replacementColor,
                "a different mod taking over the outline must not be undone by cleanup");

        aimed.setOutlineHighlightCol(0, 0.0f, 1.0f, 0.0f, 1.0f);
        aimed.setOutlineHighlight(0, true);
        int existingColor = aimed.getOutlineHighlightCol(0);
        FirearmTargetOutline.update(player, aimed.getID());
        FirearmTargetOutline.clear();
        check(aimed.isOutlineHighlight(0) && aimed.getOutlineHighlightCol(0) == existingColor,
                "an outline owned by another mod must be preserved");

        aimed.setOutlineHighlight(0, false);
        aimed.dead = true;
        FirearmTargetOutline.update(player, aimed.getID());
        check(!aimed.isOutlineHighlight(0), "a dead zombie must not get a new outline");

        System.out.println("ViewpointExtendedSupport FirearmTargetOutlineTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
