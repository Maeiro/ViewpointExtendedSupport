package maeiro.viewpointextendedsupport;

import viewpoint.core.View;
import viewpoint.interact.InteractActions;
import viewpoint.interact.LootBoxes;
import viewpoint.interact.LootTargets;
import zombie.characters.IsoPlayer;
import zombie.characters.IsoZombie;
import zombie.iso.IsoCell;
import zombie.iso.IsoGridSquare;
import zombie.iso.IsoWorld;

public final class ALifeInteractionTargetsTest {
    private ALifeInteractionTargetsTest() {
    }

    public static void run() {
        IsoCell cell = new IsoCell();
        IsoWorld.instance.currentCell = cell;
        IsoGridSquare center = new IsoGridSquare(10, 10, 0);
        IsoGridSquare nearby = new IsoGridSquare(11, 10, 0);
        IsoGridSquare outsideRange = new IsoGridSquare(13, 10, 0);
        cell.addSquare(center);
        cell.addSquare(nearby);
        cell.addSquare(outsideRange);

        IsoZombie visibleALifeNpc = new IsoZombie();
        visibleALifeNpc.x = 11.25f;
        visibleALifeNpc.y = 10.5f;
        visibleALifeNpc.setALifeUid("npc-1");
        nearby.getMovingObjects().add(visibleALifeNpc);

        nearby.getMovingObjects().add(new IsoZombie());

        IsoZombie hiddenALifeNpc = new IsoZombie();
        hiddenALifeNpc.visible = false;
        hiddenALifeNpc.setALifeUid("npc-2");
        nearby.getMovingObjects().add(hiddenALifeNpc);

        IsoZombie deadALifeNpc = new IsoZombie();
        deadALifeNpc.dead = true;
        deadALifeNpc.setALifeUid("npc-3");
        nearby.getMovingObjects().add(deadALifeNpc);

        IsoZombie distantALifeNpc = new IsoZombie();
        distantALifeNpc.setALifeUid("npc-4");
        outsideRange.getMovingObjects().add(distantALifeNpc);

        LootTargets.addedObjects.clear();
        LootBoxes.lastBox = null;
        Bridge.addALifeInteractionTargets(new IsoPlayer(), center);

        check(LootTargets.addedObjects.size() == 1,
                "only nearby, visible, living A-Life NPCs should be added as interaction targets");
        check(LootTargets.addedObjects.get(0) == visibleALifeNpc,
                "the Viewpoint person target should refer to the A-Life zombie shell");
        check(LootBoxes.lastBox != null && LootBoxes.lastBox[0] == 0,
                "the added person target should receive a collision box");
        check(Math.abs(LootBoxes.lastBox[1] - 10.95f) < 0.001f
                        && Math.abs(LootBoxes.lastBox[4] - 11.55f) < 0.001f,
                "the interaction box should be centered on the NPC shell");

        View.enabled = true;
        InteractActions.setAimed(visibleALifeNpc);
        check(Bridge.getAimedALifeNpc() == visibleALifeNpc,
                "the current Viewpoint aim should expose the matching A-Life NPC shell");
        check(Bridge.getAimedInteractionObject() == visibleALifeNpc,
                "the current Viewpoint aim should expose its raw interaction object");
        InteractActions.setAimed(new IsoZombie());
        check(Bridge.getAimedALifeNpc() == null,
                "non-A-Life zombie targets must not be exposed as A-Life HUD targets");
        check(Bridge.getAimedInteractionObject() instanceof IsoZombie,
                "the raw interaction object should remain available for Lua-side A-Life detection");
        View.enabled = false;
        InteractActions.setAimed(visibleALifeNpc);
        check(Bridge.getAimedALifeNpc() == null,
                "the A-Life HUD target must be hidden outside Viewpoint mode");
        check(Bridge.getAimedInteractionObject() == null,
                "the raw interaction object must be hidden outside Viewpoint mode");
        InteractActions.setAimed(null);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
