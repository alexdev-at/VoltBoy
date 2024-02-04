package at.alexkiefer.voltboy.components.cpu;

import at.alexkiefer.voltboy.ConnectedInternal;
import at.alexkiefer.voltboy.Tickable;
import at.alexkiefer.voltboy.VoltBoy;
import at.alexkiefer.voltboy.util.BitUtils;

public class DMA extends ConnectedInternal implements Tickable {

    private int addr;
    private int scheduleDma;
    private boolean active;

    public DMA(VoltBoy gb) {
        super(gb);
    }

    public void scheduleDmaTransfer(int addr) {
        this.addr = addr;
        scheduleDma = 2;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public void tick() {

        if(scheduleDma > 0) {
            scheduleDma--;
            if(scheduleDma == 0) {
                active = true;
            }
        }

        if(active) {

            int dest = 0xFE00 | (addr & 0xFF);
            gb.getDataBus().writeUnrestricted(dest, gb.getDataBus().readUnrestricted(addr++));

            if(dest == 0xFE9F) {
                active = false;
                addr = 0;
            }

        }

    }

}