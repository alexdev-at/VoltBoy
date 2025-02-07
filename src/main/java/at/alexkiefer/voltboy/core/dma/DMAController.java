package at.alexkiefer.voltboy.core.dma;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;

public class DMAController extends ConnectedInternal implements Tickable {

    private int sourceAddressStart;
    private int currentAddress;
    private int destinationAddress;
    private int dmaDelayTicks;
    private int currentData;

    private boolean active;

    public DMAController(VoltBoy gb) {
        super(gb);
    }

    public void scheduleStart(int high) {
        sourceAddressStart = high;
        dmaDelayTicks = 2;
    }

    public int getSourceAddressStart() {
        return sourceAddressStart;
    }

    public int getCurrentAddress() {
        return currentAddress;
    }

    public int getCurrentData() {
        return currentData;
    }

    public boolean isConflictOnExternalBus(int addr) {
        return ((currentAddress >= 0x0000 && currentAddress <= 0x7FFF) || (currentAddress >= 0xA000 && currentAddress <= 0xFDFF)) && ((addr >= 0x0000 && addr <= 0x7FFF) || (addr >= 0xA000 && addr <= 0xFDFF));
    }

    public boolean isConflictOnInternalBus(int addr) {
        return (currentAddress >= 0xFF00 && currentAddress <= 0xFFFF) && (addr >= 0xFF00 && addr <= 0xFFFF);
    }

    public boolean isConflictOnExternalVideoBus(int addr) {
        return (currentAddress >= 0x8000 && currentAddress <= 0x9FFF) && (addr >= 0x8000 && addr <= 0x9FFF);
    }

    public boolean isOnConflictInternalVideoBus(int addr) {
        return (addr >= 0xFE00 && addr <= 0xFEFF);
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void tick() {

        if (active && destinationAddress == 0xFE9F) {
            active = false;
        }

        if (dmaDelayTicks != 0) {
            if (--dmaDelayTicks == 0) {
                active = true;
                this.currentAddress = sourceAddressStart << 8;
            }
        }

        if (active) {

            destinationAddress = 0xFE00 | (currentAddress & 0xFF);
            currentData = gb.getMemoryBus().readUnrestricted(currentAddress++);
            gb.getMemoryBus().writeUnrestricted(destinationAddress, currentData);

        }

    }

}
