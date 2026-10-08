package maeiro.viewpointextendedsupport;

import java.util.ArrayList;

import viewpoint.core.View;
import viewpoint.input.FreeCam;

public final class CompanionDogTagProjectionTest {
    public static void main(String[] args) {
        View.enabled = false;
        check(Bridge.projectCompanionDogTag(0, 2.0f, 3.0f, 1.0f) == null,
                "projection must be disabled with Viewpoint");

        View.enabled = true;
        ArrayList<Float> position = Bridge.projectCompanionDogTag(0, 2.0f, 3.0f, 1.0f);
        check(position != null, "active Viewpoint should project a tag position");
        check(Math.abs(position.get(0) - 964.5f) < 0.001f,
                "tag projection should use Viewpoint's configured head height");
        check(Math.abs(position.get(1) - 545.5f) < 0.001f,
                "tag projection should return both screen coordinates");

        FreeCam.active = true;
        FreeCam.place = new FreeCam.Place();
        check(Bridge.projectCompanionDogTag(0, 2.0f, 3.0f, 1.0f) != null,
                "projection should support Viewpoint's free-camera placement");
        check(Bridge.projectCompanionDogTag(1, 2.0f, 3.0f, 1.0f) == null,
                "projection should safely reject inactive player indexes");

        System.out.println("ViewpointExtendedSupport CompanionDogTagProjectionTest: PASS");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
