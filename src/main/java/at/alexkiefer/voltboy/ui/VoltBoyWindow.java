package at.alexkiefer.voltboy.ui;

import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.ppu.pixel.Pixel;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class VoltBoyWindow {

    private final static int WIDTH = 800;
    private static final int HEIGHT = 800;

    private long window;

    private VoltBoy gb;
    private Pixel[][] lcd;

    private final int cyclesPerSecond = 1_048_576;
    private final int FPS = 60;
    private final int cyclesPerFrame = cyclesPerSecond / FPS;
    private final double NS_PER_FRAME = 1_000_000_000.0 / FPS;

    private long variableYieldTime, lastTime;

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);

        window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, "VoltBoy", 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwSetWindowPos(window, (GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).width() - WIDTH) / 2, (GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor()).height() - HEIGHT) / 2);

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

    }

    private void loop() {

        GL.createCapabilities();

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!GLFW.glfwWindowShouldClose(window)) {

            for (int i = 0; i < cyclesPerFrame; i++) {
                gb.tick();
            }

            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GL11.glBegin(GL11.GL_QUADS);
            for (int y = 0; y < lcd.length; y++) {
                for (int x = 0; x < lcd[y].length; x++) {
                    Pixel pixel = lcd[y][x];
                    int color = pixel.getColor();
                    float gray = 1.0f - color / 3.0f;
                    GL11.glColor3f(gray, gray, gray);

                    float x0 = -1.0f + 2.0f * x / lcd[y].length;
                    float y0 = 1.0f - 2.0f * y / lcd.length;
                    float x1 = -1.0f + 2.0f * (x + 1) / lcd[y].length;
                    float y1 = 1.0f - 2.0f * (y + 1) / lcd.length;

                    GL11.glVertex2f(x0, y0);
                    GL11.glVertex2f(x1, y0);
                    GL11.glVertex2f(x1, y1);
                    GL11.glVertex2f(x0, y1);
                }
            }
            GL11.glEnd();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            sync(FPS);
        }
    }

    /**
     * An accurate sync method that adapts automatically
     * to the system it runs on to provide reliable results.
     *
     * @param fps The desired frame rate, in frames per second
     */
    private void sync(int fps) {
        if (fps <= 0) return;

        long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
        // yieldTime + remainder micro & nano seconds if smaller than sleepTime
        long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000));
        long overSleep = 0; // time the sync goes over by

        try {
            while (true) {
                long t = System.nanoTime() - lastTime;

                if (t < sleepTime - yieldTime) {
                    Thread.sleep(1);
                } else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                } else {
                    overSleep = t - sleepTime;
                    break; // exit while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);

            // auto tune the time sync should yield
            if (overSleep > variableYieldTime) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime);
            } else if (overSleep < variableYieldTime - 200 * 1000) {
                // decrease by 2 microseconds
                variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
            }
        }
    }

    public void run(String path) {

        try {
            gb = new VoltBoy(path);
            lcd = gb.getPpu().getLcd();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        init();
        loop();

    }

}