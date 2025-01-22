package at.alexkiefer.voltboy.core.dma;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;

public class DMAController extends ConnectedInternal implements Tickable {

    private int currentAddress;
    private int dmaDelayTicks;

    private boolean active;

    public DMAController(VoltBoy gb) {
        super(gb);
    }

    public void scheduleStart(int high) {
        this.currentAddress = high << 8;
        dmaDelayTicks = 2;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void tick() {

        if (dmaDelayTicks != 0) {
            if (--dmaDelayTicks == 0) {
                active = true;
            }
        }

        if (active) {

            int destinationAddress = 0xFE00 | (currentAddress & 0xFF);
            gb.getMemoryBus().writeUnrestricted(destinationAddress, gb.getMemoryBus().readUnrestricted(currentAddress++));

            if (destinationAddress == 0xFE9F) {
                active = false;
            }

        }

    }

}
