package at.alexkiefer.voltboy.components.memory;

import at.alexkiefer.voltboy.VoltBoy;

public class ProhibitedSpace extends AddressSpace {

    public ProhibitedSpace(VoltBoy gb) {
        super(gb, 0xFEA0, 0xFEFF, false);
    }

    @Override
    public int read(int addr) {
        return 0x00;
    }

}