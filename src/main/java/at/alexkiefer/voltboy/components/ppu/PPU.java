package at.alexkiefer.voltboy.components.ppu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;

public class PPU extends ConnectedInternal implements Tickable {

    public PPU(VoltBoy gb) {
        super(gb);
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
