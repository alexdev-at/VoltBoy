package at.alexkiefer;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.fifo.Pixel;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;

public class Main {

    private static final int SCREEN_WIDTH = 160;
    private static final int SCREEN_HEIGHT = 144;
    private static final int PIXEL_SCALE = 5;

    private static final long GAMEBOY_CLOCK_SPEED = 4194304L; // Hz
    private static final long AVERAGE_CYCLES_PER_FRAME = 70224L; // Approximate

    private static long lastFrameTime = System.nanoTime();
    private static long accumulatedDelayNanos = 0L;

    private static int frameCount = 0;
    private static int fps = 0;

    public static void main(String[] args) throws InterruptedException {
        VoltBoy gb = new VoltBoy();

        JFrame frame = new JFrame("VoltBoy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                drawGameboyScreen(g, gb);
                frameCount++;
            }
        };
        panel.setPreferredSize(new Dimension(SCREEN_WIDTH * PIXEL_SCALE, SCREEN_HEIGHT * PIXEL_SCALE));

        frame.getContentPane().add(panel);

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        Timer timer = new Timer(1000, e -> {
            fps = frameCount;
            frameCount = 0;
        });
        timer.start();

        while (true) {
            long currentTime = System.nanoTime();
            long elapsedTimeNanos = currentTime - lastFrameTime;

            long cyclesToExecute = (elapsedTimeNanos * GAMEBOY_CLOCK_SPEED) / 1_000_000_000L;
            cyclesToExecute += accumulatedDelayNanos / (1_000_000_000L / GAMEBOY_CLOCK_SPEED);

            cyclesToExecute = Math.min(cyclesToExecute, 2 * AVERAGE_CYCLES_PER_FRAME);
            for (long i = 0; i < cyclesToExecute; i++) {
                gb.tick();
            }

            panel.repaint();

            long targetFrameTimeNanos = 1_000_000_000L / 60L;
            long actualFrameTimeNanos = System.nanoTime() - currentTime;
            long delayNanos = targetFrameTimeNanos - actualFrameTimeNanos;
            accumulatedDelayNanos += delayNanos;

            if (delayNanos > 0) {
                TimeUnit.NANOSECONDS.sleep(delayNanos);
            } else {
                // If behind schedule, reduce accumulated delay to avoid overcorrection
                accumulatedDelayNanos = Math.max(0, accumulatedDelayNanos - targetFrameTimeNanos);
            }

            lastFrameTime = System.nanoTime();
        }
    }

    private static void drawGameboyScreen(Graphics g, VoltBoy gb) {
        for (int y = 0; y < SCREEN_HEIGHT; y++) {
            for (int x = 0; x < SCREEN_WIDTH; x++) {
                Pixel p = gb.getPpu().getLcd()[y][x];
                switch(p.getColor()) {
                    case 0b00 -> g.setColor(Color.WHITE);
                    case 0b01 -> g.setColor(Color.LIGHT_GRAY);
                    case 0b10 -> g.setColor(Color.DARK_GRAY);
                    case 0b11 -> g.setColor(Color.BLACK);
                    case 5 -> g.setColor(Color.RED);
                }
                g.fillRect(x * PIXEL_SCALE, y * PIXEL_SCALE, PIXEL_SCALE, PIXEL_SCALE);
            }
        }

        // Draw the FPS display
        g.setColor(Color.BLACK);
        g.drawString("FPS: " + fps, 10, 20);
    }
}