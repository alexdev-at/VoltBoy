package at.alexkiefer.voltboy.components.ppu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;

public class PPU extends ConnectedInternal implements Tickable {

    private final int[][] lcdPixels;

    public PPU(VoltBoy gb) {
        super(gb);
        lcdPixels = new int[160][144];
    }

    @Override
    public void tick() {
        tickPpu();
        tickPpu();
        tickPpu();
        tickPpu();
    }

    private void tickPpu() {

    }

}
