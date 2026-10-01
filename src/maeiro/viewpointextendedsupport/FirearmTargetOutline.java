package maeiro.viewpointextendedsupport;

import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.core.textures.ColorInfo;
import zombie.iso.IsoMovingObject;
import zombie.network.fields.hit.HitInfo;

final class FirearmTargetOutline {
    private static IsoZombie outlinedTarget;
    private static int outlinedPlayerIndex;
    private static int previousColor;
    private static int appliedColor;
    private static boolean failed;

    private FirearmTargetOutline() {
    }

    static void update(Object character, int targetId) {
        if (failed) {
            return;
        }
        try {
            updateInternal(character, targetId);
        } catch (Throwable error) {
            fail(error);
        }
    }

    private static void updateInternal(Object character, int targetId) {
        if (!(character instanceof IsoPlayer) || targetId < 0) {
            clearInternal();
            return;
        }

        IsoPlayer player = (IsoPlayer) character;
        int playerIndex = player.getIndex();
        if (outlinedTarget != null && !outlinedTarget.isDead()
                && outlinedTarget.getID() == targetId
                && outlinedPlayerIndex == playerIndex) {
            if (outlinedTarget.getOutlineHighlightCol(playerIndex) == appliedColor) {
                outlinedTarget.setOutlineHighlight(playerIndex, true);
            }
            return;
        }

        clearInternal();
        IsoZombie target = findTarget(player, targetId);
        if (target == null || target.isDead() || target.isOutlineHighlight(playerIndex)) {
            return;
        }

        previousColor = target.getOutlineHighlightCol(playerIndex);
        target.setOutlineHighlightCol(playerIndex, 1.0f, 0.12f, 0.08f, 1.0f);
        appliedColor = target.getOutlineHighlightCol(playerIndex);
        target.setOutlineHighlight(playerIndex, true);
        outlinedTarget = target;
        outlinedPlayerIndex = playerIndex;
    }

    static void clear() {
        if (failed) {
            return;
        }
        try {
            clearInternal();
        } catch (Throwable error) {
            fail(error);
        }
    }

    private static void clearInternal() {
        if (outlinedTarget == null) {
            return;
        }
        if (outlinedTarget.getOutlineHighlightCol(outlinedPlayerIndex) == appliedColor) {
            outlinedTarget.setOutlineHighlight(outlinedPlayerIndex, false);
            outlinedTarget.setOutlineHighlightCol(outlinedPlayerIndex,
                    new ColorInfo(0.0f, 0.0f, 0.0f, 0.0f).setABGR(previousColor));
        }
        outlinedTarget = null;
    }

    private static void fail(Throwable error) {
        failed = true;
        outlinedTarget = null;
        System.out.println("[Viewpoint Extended Support] target outline disabled after error");
        error.printStackTrace(System.out);
    }

    private static IsoZombie findTarget(IsoPlayer player, int targetId) {
        if (player.getHitInfoList() != null) {
            for (HitInfo hit : player.getHitInfoList()) {
                if (hit == null) {
                    continue;
                }
                IsoMovingObject object = hit.getObject();
                if (object instanceof IsoZombie && object.getID() == targetId) {
                    return (IsoZombie) object;
                }
            }
        }
        if (player.getCell() != null) {
            for (IsoZombie zombie : player.getCell().getZombieList()) {
                if (zombie != null && zombie.getID() == targetId) {
                    return zombie;
                }
            }
        }
        return null;
    }
}
