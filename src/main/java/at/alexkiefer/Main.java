package at.alexkiefer;

import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.components.ppu.fifo.Pixel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Main {

    private static final int SCREEN_WIDTH = 160;
    private static final int SCREEN_HEIGHT = 144;
    private static final int PIXEL_SCALE = 5;

    public static void main(String[] args) {
        VoltBoy gb = new VoltBoy();

        JFrame frame = new JFrame("VoltBoy");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                drawGameboyScreen(g, gb);
            }
        };
        panel.setSize(SCREEN_WIDTH * PIXEL_SCALE, SCREEN_HEIGHT * PIXEL_SCALE);

        frame.setSize(panel.getSize());

        frame.getContentPane().add(panel);

        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        while (true) {
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
                }
                g.fillRect(x * PIXEL_SCALE, y * PIXEL_SCALE, PIXEL_SCALE, PIXEL_SCALE);
            }
        }
    }
}
