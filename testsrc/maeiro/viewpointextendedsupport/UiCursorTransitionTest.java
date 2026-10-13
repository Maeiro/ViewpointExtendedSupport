package maeiro.viewpointextendedsupport;

import org.lwjgl.glfw.GLFW;
import org.lwjglx.opengl.Display;
import viewpoint.FP;
import viewpoint.core.View;
import viewpoint.input.Look;
import zombie.input.GameKeyboard;
import zombie.input.Mouse;
import zombie.iso.IsoCamera;

public final class UiCursorTransitionTest {
    public static void main(String[] args) {
        View.enabled = true;
        FP.cursorMode = false;
        GameKeyboard.down.clear();
        Bridge.configure(true, 56, true, false, false, true, 0);
        Look.captured = true;
        GLFW.mode = 212995;
        movePointer(3919, 610);
        Mouse.absoluteX = 979;
        Mouse.absoluteY = 396;

        Bridge.setAutoCursorRequested(true);
        updateCursor();
        checkCentered("opening captured UI must center the pointer, not restore old game coordinates");
        check(GLFW.cursorPositionCalls == 1, "Viewpoint's native centering must not be duplicated");

        movePointer(723, 412);
        Mouse.absoluteX = 1919;
        Mouse.absoluteY = 609;
        for (int frame = 0; frame < 10; frame++) {
            Bridge.setAutoCursorRequested(true);
            Bridge.prepareMouseCursorUpdate();
            publishCaptureRequest();
            check(!Look.wantCapture, "Viewpoint must not request capture after ES releases an open UI cursor");
            check(!FP.cursorMode, "automatic UI release must preserve Viewpoint's native toggle state");
            boolean original = Look.onUpdateMouseCursor();
            Bridge.overrideLookMouseCursorUpdate(original);
            updateCursor();
        }
        checkPointer(723, 412, "an open UI must not warp the pointer or re-enable camera capture");
        check(GLFW.cursorPositionCalls == 1, "stable UI requests must not repeatedly center");

        FP.toggleCursorMode();
        publishCaptureRequest();
        check(FP.cursorMode && !Look.wantCapture, "a native cursor toggle must survive automatic UI support");
        FP.toggleCursorMode();
        GameKeyboard.down.add(56);
        publishCaptureRequest();
        GameKeyboard.down.remove(56);
        publishCaptureRequest();
        check(!FP.cursorMode && !Look.wantCapture, "releasing the hold key must not recapture an open UI cursor");

        Bridge.setAutoCursorRequested(false);
        updateCursor();
        check(Look.captured, "closing UI must allow camera capture again");
        movePointer(-4000, 9000);
        Bridge.setAutoCursorRequested(true);
        updateCursor();
        checkCentered("reopening UI after camera movement must center again");
        check(GLFW.cursorPositionCalls == 2, "each UI opening must center only once");

        Bridge.setAutoCursorRequested(false);
        FP.cursorMode = true;
        Look.captured = false;
        GLFW.mode = 212993;
        movePointer(123, 867);
        Bridge.setAutoCursorRequested(true);
        updateCursor();
        checkCentered("opening UI with an already free cursor must still center");
        check(GLFW.cursorPositionCalls == 3, "already free cursor needs one explicit centering");

        Bridge.setAutoCursorRequested(false);
        View.thirdPerson = true;
        IsoCamera.Character player = (IsoCamera.Character) IsoCamera.character;
        player.vehicle = new Object();
        Display.width = 1281;
        Display.height = 721;
        movePointer(15, 650);
        Bridge.setAutoCursorRequested(true);
        updateCursor();
        checkCentered("vehicle UI must use the actual window center after a resize");
        movePointer(1100, 90);
        updateCursor();
        checkPointer(1100, 90, "moving the pointer in vehicle UI must not mirror or reset it");
        check(GLFW.cursorPositionCalls == 4, "vehicle UI must not continually warp the pointer");
        player.vehicle = null;

        Bridge.setAutoCursorRequested(false);
        Bridge.configure(false, 56, true, false, false, true, 0);
        Look.captured = true;
        GLFW.mode = 212995;
        Bridge.setAutoCursorRequested(true);
        updateCursor();
        checkCentered("automatic UI release must work with hold-to-use disabled");
        movePointer(150, 300);
        updateCursor();
        checkPointer(150, 300, "toggle mode must also leave an open UI pointer alone");
        check(GLFW.cursorPositionCalls == 5, "toggle mode must not recapture while UI stays open");

        Bridge.setAutoCursorRequested(false);
        FP.cursorMode = true;
        Bridge.setAutoCursorRequested(true);
        Bridge.setAutoCursorRequested(false);
        movePointer(301, 123);
        updateCursor();
        checkPointer(301, 123, "closing before the native update must cancel pending centering");

        View.enabled = false;
        Look.captured = false;
        GLFW.mode = 212993;
        Bridge.setAutoCursorRequested(true);
        updateCursor();
        checkPointer(301, 123, "ES must not warp the vanilla cursor outside Viewpoint");
        Bridge.setAutoCursorRequested(false);
        View.enabled = true;
        Bridge.configure(false, 56, false, false, false, true, 0);
        Bridge.setAutoCursorRequested(true);
        updateCursor();
        check(Look.captured, "disabled automatic UI support must not release camera capture");
        check(GLFW.cursorPositionCalls == 5, "disabled support must not perform centering");

        System.out.println("UiCursorTransitionTest: PASS");
    }

    private static void updateCursor() {
        publishCaptureRequest();
        Bridge.prepareMouseCursorUpdate();
        boolean original = Look.onUpdateMouseCursor();
        check(Bridge.overrideLookMouseCursorUpdate(original) == original,
                "ES must preserve the native cursor update result");
    }

    private static void publishCaptureRequest() {
        Patches.CursorMode.exit();
        try {
            Look.wantCapture = !FP.cursorMode;
        } finally {
            Patches.CursorCaptureRequest.exit();
        }
    }

    private static void movePointer(double x, double y) {
        GLFW.cursorX = x;
        GLFW.cursorY = y;
    }

    private static void checkCentered(String message) {
        check(!Look.captured && GLFW.mode == 212993, "UI cursor must be released before centering");
        checkPointer(Display.getWidth() / 2.0, Display.getHeight() / 2.0, message);
    }

    private static void checkPointer(double x, double y, String message) {
        check(GLFW.cursorX == x && GLFW.cursorY == y, message);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
