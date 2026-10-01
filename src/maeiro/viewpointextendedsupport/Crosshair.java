package maeiro.viewpointextendedsupport;

import org.lwjgl.opengl.GL11;

final class Crosshair {
    private static final int[] VIEWPORT = new int[4];
    private static boolean failed;

    private Crosshair() {
    }

    static boolean draw(boolean target) {
        if (failed) {
            return false;
        }
        try {
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, VIEWPORT);
            if (VIEWPORT[2] <= 0 || VIEWPORT[3] <= 0) {
                return false;
            }

            int x = VIEWPORT[0] + VIEWPORT[2] / 2;
            int y = VIEWPORT[1] + VIEWPORT[3] / 2;
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            clear(x - 2, y - 2, 4, 4);
            clear(x - 10, y - 2, 9, 4);
            clear(x + 1, y - 2, 9, 4);
            clear(x - 2, y - 10, 4, 9);
            clear(x - 2, y + 1, 4, 9);

            if (target) {
                GL11.glClearColor(0.22f, 0.18f, 1.0f, 1.0f);
            } else {
                GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            clear(x - 1, y - 1, 2, 2);
            clear(x - 9, y - 1, 7, 2);
            clear(x + 2, y - 1, 7, 2);
            clear(x - 1, y - 9, 2, 7);
            clear(x - 1, y + 2, 2, 7);
            return true;
        } catch (Throwable error) {
            failed = true;
            System.out.println("[Viewpoint Extended Support] crosshair draw failed; using Viewpoint fallback");
            error.printStackTrace(System.out);
            return false;
        }
    }

    private static void clear(int x, int y, int width, int height) {
        GL11.glScissor(x, y, width, height);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    }
}
