package at.alexkiefer.voltboy.core.ppu;

import at.alexkiefer.voltboy.core.ConnectedInternal;
import at.alexkiefer.voltboy.core.Tickable;
import at.alexkiefer.voltboy.core.VoltBoy;
import at.alexkiefer.voltboy.core.memory.MemoryBus;
import at.alexkiefer.voltboy.core.ppu.object.OAMObject;
import at.alexkiefer.voltboy.core.ppu.object.OAMObjectAttributes;
import at.alexkiefer.voltboy.util.BitMasks;

public class PPU extends ConnectedInternal implements Tickable {

    private MemoryBus memoryBus;

    private int dot;

    private PPUMode mode;

    private int lx;
    private int ly;

    private OAMObject[] oamBuffer;
    private int oamIndex;
    private int oamAddress;

    public PPU(VoltBoy gb) {

        super(gb);
        memoryBus = gb.getMemoryBus();

        mode = PPUMode.MODE_2_OAMSCAN;

        lx = 0;
        ly = 0;

        oamBuffer = new OAMObject[10];
        oamIndex = 0;
        oamAddress = 0xFE00;

    }

    @Override
    public void tick() {

        if ((memoryBus.readUnrestricted(0xFF40) & BitMasks.SEVEN) == 0) {
            return;
        }

        tickPpu();
        tickPpu();
        tickPpu();
        tickPpu();

    }

    private void tickPpu() {

        if (dot == 0) {
            oamAddress = 0xFE00;
            oamIndex = 0;
        }

        switch (mode) {
            case MODE_0_HBLANK, MODE_1_VBLANK -> {}
            case MODE_2_OAMSCAN -> oamScan();
            case MODE_3_RENDERING -> {}
        }

    }

    private void oamScan() {

        if (dot % 2 != 0) {
            return;
        }

        int objectY = memoryBus.readUnrestricted(oamAddress++);
        int objectX = memoryBus.readUnrestricted(oamAddress++);
        int objectTileIndex = memoryBus.readUnrestricted(oamAddress++);
        OAMObjectAttributes objectAttributes = new OAMObjectAttributes(memoryBus.readUnrestricted(oamAddress++));

        int size = (memoryBus.readUnrestricted(0xFF40) & BitMasks.TWO) == 0 ? 8 : 16;
        if((ly + 16) >= objectY && (ly + 16) < (objectY + size) && oamIndex < 10) {
            oamBuffer[oamIndex++] = new OAMObject(objectX, objectY, objectTileIndex, objectAttributes);
        }

    }

}
