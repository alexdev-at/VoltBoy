package at.alexkiefer;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.PPUMode;
import at.alexkiefer.voltboy.components.ppu.fifo.Pixel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    private static final int SCREEN_WIDTH = 160;
    private static final int SCREEN_HEIGHT = 144;
    private static final int PIXEL_SCALE = 5;

    public static void main(String[] args) throws InterruptedException {
        VoltBoy gb = new VoltBoy();

        JFrame frame = new JFrame("VoltBoy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                drawGameboyScreen(g, gb);
            }
        };
        panel.setPreferredSize(new Dimension(SCREEN_WIDTH * PIXEL_SCALE, SCREEN_HEIGHT * PIXEL_SCALE));

        frame.getContentPane().add(panel);

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        gb.getInputHandler().pressJoypadUp();
                        break;
                    case KeyEvent.VK_S:
                        gb.getInputHandler().pressJoypadDown();
                        break;
                    case KeyEvent.VK_A:
                        gb.getInputHandler().pressJoypadLeft();
                        break;
                    case KeyEvent.VK_D:
                        gb.getInputHandler().pressJoypadRight();
                        break;
                    case KeyEvent.VK_SPACE:
                        gb.getInputHandler().pressSelect();
                        break;
                    case KeyEvent.VK_ENTER:
                        gb.getInputHandler().pressStart();
                        break;
                    case KeyEvent.VK_LEFT:
                        gb.getInputHandler().pressA();
                        break;
                    case KeyEvent.VK_RIGHT:
                        gb.getInputHandler().pressB();
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        gb.getInputHandler().releaseJoypadUp();
                        break;
                    case KeyEvent.VK_S:
                        gb.getInputHandler().releaseJoypadDown();
                        break;
                    case KeyEvent.VK_A:
                        gb.getInputHandler().releaseJoypadLeft();
                        break;
                    case KeyEvent.VK_D:
                        gb.getInputHandler().releaseJoypadRight();
                        break;
                    case KeyEvent.VK_SPACE:
                        gb.getInputHandler().releaseSelect();
                        break;
                    case KeyEvent.VK_ENTER:
                        gb.getInputHandler().releaseStart();
                        break;
                    case KeyEvent.VK_LEFT:
                        gb.getInputHandler().releaseA();
                        break;
                    case KeyEvent.VK_RIGHT:
                        gb.getInputHandler().releaseB();
                        break;
                }
            }
        });

        frame.setFocusable(true);
        frame.requestFocusInWindow();

        while(true) {
            gb.tick();
            panel.repaint();
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
    }
}