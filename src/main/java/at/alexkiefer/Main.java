package at.alexkiefer;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.fifo.Pixel;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;

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